# This file contains shell functions for the account API.
# It's based on HTTPie (https://httpie.org/) which a very convenient console http client.
# To use those funcitons you most 'source' this file on your shell environment.
# e.g.: . api.sh
# Functions will be available in your shell prompt

# Description: gets the balance of the passed account. 
# 
# Use case: get the balance for account 123-4567-890
# Call: account.balance 123-4567-890
account.balance() {
  http localhost:9000/api/account/$1/balance
}

# Description:  adds a depoist on a given account. 
# 
# Use case: deposit 100 on account 123-4567-890
# Call: account.deposit 123-4567-890 100
account.deposit() {
  http POST localhost:9000/api/account/$1/deposit amount=$2
}


# Description:  adds a withdraw on a given account. 
# 
# Use case: withdraw 100 from account 123-4567-890
# Call: account.withdraw 123-4567-890 100
account.withdraw() {  
  http POST localhost:9000/api/account/$1/withdraw amount=$2
}


# Description:  retrive an account extract
# 
# Use case: retrieve extrat #1 from account 123-4567-890
# Call: account.extract 123-4567-890 1
account.extract() {
  http localhost:9000/api/account/$1/extract/$2
}
