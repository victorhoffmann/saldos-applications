# saldos-applications

## Banco de dados

Optei pelo postgres, pensando na leitura e escrita separada, pesquisei que ele trabalha muito bem nesse cenário

## Ingestão

Dentro do TransactionEventDTO utilizei o @Bean do jakarta para validações como: NotNull, NotBlank, DecimalMin, Size, Valid.

Como não tenho regras de negócio, optei por não utilizar muitas camadas. Se o projeto crescer (regras de negócio, integrações, testes mais complexos), então sim adicionaria mais camadas/padrões. 