#! /bin/bash
DBNAME=$MDELA022_DB
PORT=$7567
USER=$MDELA022 

# Example: source ./run.sh flightDB 5432 user
java -cp lib/*:bin/ MechanicShop $DBNAME $PORT $USER
