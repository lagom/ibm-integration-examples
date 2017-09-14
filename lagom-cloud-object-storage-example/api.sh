
account.balance() {
  http localhost:9000/api/account/$1/balance
}


account.deposit() {
  http POST localhost:9000/api/account/$1/deposit amount=$2
}


account.withdraw() {  
  http POST localhost:9000/api/account/$1/withdraw amount=$2
}

account.extract() {
  http localhost:9000/api/account/$1/extract/$2
}
