# This file contains shell functions for the account API.
# It's based on HTTPie (https://httpie.org/) which is a very convenient console http client.
#
# To use is you must 'source' this file on your shell environment and call the available functions from your shell environment.
# e.g.: . api.sh

# Use case: get the balance for account 123-4567-890
# Call: account.balance 123-4567-890
account.balance() {
  http localhost:9000/api/account/$1/balance
}

# Use case: deposit 100 on account 123-4567-890
# Call: account.deposit 123-4567-890 100
account.deposit() {
  http POST localhost:9000/api/account/$1/deposit amount=$2
}


# Use case: withdraw 100 from account 123-4567-890
# Call: account.withdraw 123-4567-890 100
account.withdraw() {  
  http POST localhost:9000/api/account/$1/withdraw amount=$2
}


# Use case: retrieve extract #1 from account 123-4567-890
# Call: account.extract 123-4567-890 1
account.extract() {
  http localhost:9000/api/account/$1/extract/$2
}
