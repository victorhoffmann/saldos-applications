#!/bin/bash
set -e

# Função para esperar um serviço HTTP estar disponível
wait_for_http() {
  local url=$1
  local retries=${2:-12}  # default 12 tentativas
  local wait=${3:-5}      # default 5 segundos entre tentativas

  echo "Esperando serviço $url ficar disponível..."
  for i in $(seq 1 $retries); do
    if curl -s $url > /dev/null; then
      echo "$url disponível!"
      return 0
    else
      echo "Ainda não disponível, tentando novamente em $wait s..."
      sleep $wait
    fi
  done

  echo "Falha: $url não ficou disponível após $retries tentativas."
  exit 1
}

# Espera Localstack
wait_for_http "$SQS_ENDPOINT/_localstack/health"

# Testa se fila está acessível
echo "Verificando fila SQS..."
for i in $(seq 1 12); do
  if aws --endpoint-url=$SQS_ENDPOINT sqs get-queue-url --queue-name transacoes-financeiras-processadas > /dev/null 2>&1; then
    echo "Fila SQS disponível!"
    break
  else
    echo "Fila ainda não disponível, aguardando 5s..."
    sleep 5
  fi
done

# Inicia a aplicação Java
echo "Iniciando ingestao-app..."
exec java \
  -Xms2048m -Xmx4096m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:InitiatingHeapOccupancyPercent=45 \
  -XX:MaxMetaspaceSize=512m \
  -XX:+ParallelRefProcEnabled \
  -jar app.jar
