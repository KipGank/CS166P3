#! /bin/bash
DBNAME=$MDELA022_DB
PORT=$8465
USER=$MDELA022 

# Example: source ./run.sh flightDB 5432 user
cat <(echo 'CREATE SEQUENCE part_number_seq  START WITH 30000;')|psql -h 127.0.0.1 -p $PGPORT $USER"_DB"
java -cp lib/*:bin/ MechanicShop $DBNAME $PORT $USER
