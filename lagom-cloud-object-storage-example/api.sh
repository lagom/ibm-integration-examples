
account.balance() {
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

account.report() {
  http localhost:9000/api/account/$1/report/$2 --verbose
}
