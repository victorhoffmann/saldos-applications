# saldos-applications

## Banco de dados

Optei pelo postgres, pensando na leitura e escrita separada, pesquisei que ele trabalha muito bem nesse cenário
Tentei realizar a replicação mas não consegui fazer funcionar a replicação via docker, então irei utilizar nesse desafio apenas um para escrita/leitura por conta dessa limitação que acabei esbarrando

docker exec -it saldos-applications-postgres_primary-1 psql -U postgres -d contas
docker exec -it saldos-applications-postgres_replica-1 psql -U postgres -d contas

docker exec -it saldos-applications-postgres_replica-1 psql -U postgres -d contas -c "\watch 2 SELECT id, owner, balance_amount, balance_currency, created_at FROM accounts ORDER BY created_at DESC LIMIT 5;"


Consegui ativar a replicação (repo que me salvou: https://github.com/eremeykin/pg-primary-replica)

## Ingestão

Dentro do TransactionEventDTO utilizei o @Bean do jakarta para validações como: NotNull, NotBlank, DecimalMin, Size, Valid.

Como não tenho regras de negócio, optei por não utilizar muitas camadas. Se o projeto crescer (regras de negócio, integrações, testes mais complexos), então sim adicionaria mais camadas/padrões. 

Estou utilizando o SQS Listener para consumir as mensagens da fila com 10 concurrency, 10 messages por poll, 30 segundos de wait time e acknowledgement manual.