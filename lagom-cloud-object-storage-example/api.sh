
account.get() {
  http localhost:9000/api/account/$1/balance --verbose
}


account.deposit() {

cat << EOF > /tmp/last.json
{
  "amount": "$2"
}
EOF
  
  http POST localhost:9000/api/account/$1/deposit --verbose < /tmp/last.json
}


account.withdraw() {

cat << EOF > /tmp/last.json
{
  "amount": "$2"
}
EOF
  
  http POST localhost:9000/api/account/$1/withdraw --verbose < /tmp/last.json
}

account.reports() {
  http localhost:9000/api/account/$1/reports --verbose
}
