#!/bin/bash
set -e

# Verifica se o diretório de dados estiver vazio, então inicializa com base backup
if [ -z "$(ls -A /var/lib/postgresql/data)" ]; then
    echo "Inicializando replica com base backup..."

    until pg_basebackup \
      --pgdata=/var/lib/postgresql/data \
      -R \
      --slot=replication_slot \
      --host=postgres_primary \
      --port=5432 \
      -U replicator \
      -W \
      -P
    do
      echo "Esperando para conectar..."
      sleep 1s
    done

    echo "Backup concluído, ajustando permissões..."
    chmod 0700 /var/lib/postgresql/data
else
    echo "Replica já inicializada, pulando base backup."
fi

exec postgres
