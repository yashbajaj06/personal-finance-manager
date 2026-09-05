#!/bin/bash

BASE_URL="$1"

if [ -z "$BASE_URL" ]; then
  echo "Usage: bash pfm_smoke_tests.sh <base_url>"
  echo "Example: bash pfm_smoke_tests.sh https://personal-finance-manager-1-sihc.onrender.com/api"
  exit 1
fi

BASE_URL="${BASE_URL%/}"

PASS=0
FAIL=0
COOKIE_JAR_A="$(mktemp)"
COOKIE_JAR_B="$(mktemp)"
RESP_FILE="$(mktemp)"

RAND=$RANDOM
EMAIL_A="smoketest_a_${RAND}@example.com"
EMAIL_B="smoketest_b_${RAND}@example.com"
PASSWORD="password123"

check() {
  local description="$1"
  local expected="$2"
  local actual="$3"
  if [ "$expected" == "$actual" ]; then
    echo "PASS - $description (expected $expected, got $actual)"
    PASS=$((PASS+1))
  else
    echo "FAIL - $description (expected $expected, got $actual)"
    FAIL=$((FAIL+1))
  fi
}

extract_field() {
  local body="$1"
  local field="$2"
  echo "$body" | grep -o "\"$field\"[[:space:]]*:[[:space:]]*[0-9]*" | head -1 | grep -o '[0-9]*$'
}

req() {
  local method="$1"
  local url="$2"
  local jar="$3"
  local body="$4"
  if [ -n "$body" ]; then
    curl -s --max-time 90 -o "$RESP_FILE" -w "%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json" -b "$jar" -c "$jar" -d "$body"
  else
    curl -s --max-time 90 -o "$RESP_FILE" -w "%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json" -b "$jar" -c "$jar"
  fi
}

echo "================================================"
echo "Personal Finance Manager - Fallback Smoke Tests"
echo "Base URL: $BASE_URL"
echo "This is NOT the official grading script - it is a"
echo "best-effort sanity check covering the same flows."
echo "================================================"
echo

echo "--- Auth ---"

CODE=$(req POST "$BASE_URL/auth/register" "$COOKIE_JAR_A" "{\"username\":\"$EMAIL_A\",\"password\":\"$PASSWORD\",\"fullName\":\"Smoke Test A\",\"phoneNumber\":\"+1234567890\"}")
check "Register user A" "201" "$CODE"

CODE=$(req POST "$BASE_URL/auth/register" "$COOKIE_JAR_A" "{\"username\":\"$EMAIL_A\",\"password\":\"$PASSWORD\",\"fullName\":\"Smoke Test A\",\"phoneNumber\":\"+1234567890\"}")
check "Duplicate register returns 409" "409" "$CODE"

CODE=$(req POST "$BASE_URL/auth/register" "$COOKIE_JAR_A" "{\"username\":\"not-an-email\",\"password\":\"$PASSWORD\",\"fullName\":\"Bad\",\"phoneNumber\":\"+1234567890\"}")
check "Invalid email register returns 400" "400" "$CODE"

CODE=$(req POST "$BASE_URL/auth/login" "$COOKIE_JAR_A" "{\"username\":\"$EMAIL_A\",\"password\":\"wrong-password\"}")
check "Login with wrong password returns 401" "401" "$CODE"

CODE=$(req POST "$BASE_URL/auth/login" "$COOKIE_JAR_A" "{\"username\":\"$EMAIL_A\",\"password\":\"$PASSWORD\"}")
check "Login with correct credentials" "200" "$CODE"

CODE=$(req GET "$BASE_URL/categories" "$COOKIE_JAR_B" "")
check "Unauthenticated request returns 401" "401" "$CODE"

echo
echo "--- Categories ---"

CODE=$(req GET "$BASE_URL/categories" "$COOKIE_JAR_A" "")
check "Get categories" "200" "$CODE"
CATBODY=$(cat "$RESP_FILE")
if echo "$CATBODY" | grep -q '"id"'; then
  echo "PASS - Category response includes an id field"
  PASS=$((PASS+1))
else
  echo "FAIL - Category response is missing an id field"
  FAIL=$((FAIL+1))
fi

FOOD_ID=$(echo "$CATBODY" | grep -o '{[^}]*"name"[[:space:]]*:[[:space:]]*"Food"[^}]*}' | grep -o '"id"[[:space:]]*:[[:space:]]*[0-9]*' | grep -o '[0-9]*')

CODE=$(req POST "$BASE_URL/categories" "$COOKIE_JAR_A" "{\"name\":\"SideHustle_${RAND}\",\"type\":\"INCOME\"}")
check "Create custom category" "201" "$CODE"

CODE=$(req POST "$BASE_URL/categories" "$COOKIE_JAR_A" "{\"name\":\"SideHustle_${RAND}\",\"type\":\"INCOME\"}")
check "Duplicate custom category returns 409" "409" "$CODE"

CODE=$(req DELETE "$BASE_URL/categories/Salary" "$COOKIE_JAR_A" "")
check "Deleting a default category is rejected" "403" "$CODE"

echo
echo "--- Transactions ---"

TODAY=$(date +%Y-%m-%d)
FUTURE_DATE=$(date -d "+30 days" +%Y-%m-%d 2>/dev/null || date -v+30d +%Y-%m-%d 2>/dev/null || echo "2099-01-01")

CODE=$(req POST "$BASE_URL/transactions" "$COOKIE_JAR_A" "{\"amount\":50000,\"date\":\"$TODAY\",\"category\":\"Salary\",\"description\":\"Smoke test salary\"}")
check "Create income transaction" "201" "$CODE"
INCOME_ID=$(extract_field "$(cat "$RESP_FILE")" "id")

CODE=$(req POST "$BASE_URL/transactions" "$COOKIE_JAR_A" "{\"amount\":300,\"date\":\"$TODAY\",\"category\":\"Food\",\"description\":\"Smoke test groceries\"}")
check "Create expense transaction" "201" "$CODE"
EXPENSE_ID=$(extract_field "$(cat "$RESP_FILE")" "id")

CODE=$(req POST "$BASE_URL/transactions" "$COOKIE_JAR_A" "{\"amount\":100,\"date\":\"$FUTURE_DATE\",\"category\":\"Food\"}")
check "Future-dated transaction returns 400" "400" "$CODE"

CODE=$(req POST "$BASE_URL/transactions" "$COOKIE_JAR_A" "{\"amount\":-50,\"date\":\"$TODAY\",\"category\":\"Food\"}")
check "Negative amount returns 400" "400" "$CODE"

CODE=$(req GET "$BASE_URL/transactions?type=INCOME" "$COOKIE_JAR_A" "")
check "Filter transactions by type" "200" "$CODE"
if [ -n "$INCOME_ID" ] && echo "$(cat "$RESP_FILE")" | grep -q "\"id\":$INCOME_ID"; then
  echo "PASS - type=INCOME filter includes the income transaction"
  PASS=$((PASS+1))
else
  echo "FAIL - type=INCOME filter did not return expected transaction"
  FAIL=$((FAIL+1))
fi

if [ -n "$FOOD_ID" ]; then
  CODE=$(req GET "$BASE_URL/transactions?categoryId=$FOOD_ID" "$COOKIE_JAR_A" "")
  check "Filter transactions by categoryId" "200" "$CODE"
fi

CODE=$(req GET "$BASE_URL/transactions?startDate=$TODAY&endDate=$TODAY" "$COOKIE_JAR_A" "")
check "Filter transactions by date range" "200" "$CODE"

if [ -n "$EXPENSE_ID" ]; then
  CODE=$(req PUT "$BASE_URL/transactions/$EXPENSE_ID" "$COOKIE_JAR_A" "{\"amount\":350,\"description\":\"Updated groceries\"}")
  check "Update transaction" "200" "$CODE"
fi

CODE=$(req PUT "$BASE_URL/transactions/999999999" "$COOKIE_JAR_A" "{\"amount\":100}")
check "Update nonexistent transaction returns 404" "404" "$CODE"

CODE=$(req DELETE "$BASE_URL/transactions/999999999" "$COOKIE_JAR_A" "")
check "Delete nonexistent transaction returns 404" "404" "$CODE"

CODE=$(req GET "$BASE_URL/transactions/$EXPENSE_ID" "$COOKIE_JAR_A" "")
check "Unsupported method (GET) on transactions/{id} does not 500" "404" "$CODE"

echo
echo "--- Savings Goals ---"

START_DATE=$(date +%Y-%m-%d)
TARGET_DATE=$(date -d "+180 days" +%Y-%m-%d 2>/dev/null || date -v+180d +%Y-%m-%d 2>/dev/null || echo "2027-01-01")

CODE=$(req POST "$BASE_URL/goals" "$COOKIE_JAR_A" "{\"goalName\":\"Smoke Test Fund\",\"targetAmount\":5000,\"targetDate\":\"$TARGET_DATE\",\"startDate\":\"$START_DATE\"}")
check "Create savings goal" "201" "$CODE"
GOAL_ID=$(extract_field "$(cat "$RESP_FILE")" "id")

CODE=$(req POST "$BASE_URL/goals" "$COOKIE_JAR_A" "{\"goalName\":\"Bad Goal\",\"targetAmount\":5000,\"targetDate\":\"$TODAY\",\"startDate\":\"$TODAY\"}")
check "Goal with past/today target date returns 400" "400" "$CODE"

if [ -n "$GOAL_ID" ]; then
  CODE=$(req GET "$BASE_URL/goals/$GOAL_ID" "$COOKIE_JAR_A" "")
  check "Get goal by id" "200" "$CODE"

  CODE=$(req PUT "$BASE_URL/goals/$GOAL_ID" "$COOKIE_JAR_A" "{\"targetAmount\":6000,\"targetDate\":\"$TARGET_DATE\"}")
  check "Update goal" "200" "$CODE"
fi

echo
echo "--- Reports ---"

YEAR=$(date +%Y)
MONTH=$(date +%-m 2>/dev/null || date +%m | sed 's/^0*//')

CODE=$(req GET "$BASE_URL/reports/monthly/$YEAR/$MONTH" "$COOKIE_JAR_A" "")
check "Monthly report" "200" "$CODE"

CODE=$(req GET "$BASE_URL/reports/yearly/$YEAR" "$COOKIE_JAR_A" "")
check "Yearly report" "200" "$CODE"

echo
echo "--- Data isolation (second user) ---"

CODE=$(req POST "$BASE_URL/auth/register" "$COOKIE_JAR_B" "{\"username\":\"$EMAIL_B\",\"password\":\"$PASSWORD\",\"fullName\":\"Smoke Test B\",\"phoneNumber\":\"+1234567890\"}")
check "Register user B" "201" "$CODE"

CODE=$(req POST "$BASE_URL/auth/login" "$COOKIE_JAR_B" "{\"username\":\"$EMAIL_B\",\"password\":\"$PASSWORD\"}")
check "Login user B" "200" "$CODE"

if [ -n "$GOAL_ID" ]; then
  CODE=$(req GET "$BASE_URL/goals/$GOAL_ID" "$COOKIE_JAR_B" "")
  check "User B accessing user A's goal returns 403" "403" "$CODE"
fi

CODE=$(req GET "$BASE_URL/transactions" "$COOKIE_JAR_B" "")
check "User B's transaction list is empty (data isolation)" "200" "$CODE"
if echo "$(cat "$RESP_FILE")" | grep -q "\"transactions\":\[\]"; then
  echo "PASS - user B sees no transactions from user A"
  PASS=$((PASS+1))
else
  echo "FAIL - user B's transaction list is not empty, data isolation may be broken"
  FAIL=$((FAIL+1))
fi

echo
echo "--- Cleanup / Delete ---"

if [ -n "$EXPENSE_ID" ]; then
  CODE=$(req DELETE "$BASE_URL/transactions/$EXPENSE_ID" "$COOKIE_JAR_A" "")
  check "Delete transaction" "200" "$CODE"
fi

if [ -n "$GOAL_ID" ]; then
  CODE=$(req DELETE "$BASE_URL/goals/$GOAL_ID" "$COOKIE_JAR_A" "")
  check "Delete goal" "200" "$CODE"
fi

echo
echo "--- Logout ---"

CODE=$(req POST "$BASE_URL/auth/logout" "$COOKIE_JAR_A" "")
check "Logout" "200" "$CODE"

CODE=$(req GET "$BASE_URL/transactions" "$COOKIE_JAR_A" "")
check "Request after logout returns 401" "401" "$CODE"

rm -f "$COOKIE_JAR_A" "$COOKIE_JAR_B" "$RESP_FILE"

echo
echo "================================================"
echo "SMOKE TEST SUMMARY"
echo "================================================"
echo "Passed: $PASS"
echo "Failed: $FAIL"
TOTAL=$((PASS+FAIL))
echo "Total:  $TOTAL"
if [ "$FAIL" -eq 0 ]; then
  echo "All smoke tests passed."
else
  echo "Some smoke tests failed - review the FAIL lines above."
fi
echo "Reminder: this is a fallback sanity check, not the"
echo "official assignment grading script."
