#!/bin/bash
###############################################################################
# RALPH LOOP — Orquestrador
#
# Executa o Ralph em worktree isolado com Skinner Enforcement.
# Uso:
#   ./ralph-loop.sh                        # Modo normal (até 20 loops)
#   ./ralph-loop.sh --max-loops 5          # Limitar a 5 loops
#   ./ralph-loop.sh --dry-run              # Simular sem executar
#   ./ralph-loop.sh --prompt "fix auth"    # Task específica
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

if [ ! -f "$SCRIPT_DIR/.skinner/skinner.sh" ]; then
    echo "ERROR: Skinner not found at .skinner/skinner.sh"
    exit 1
fi

exec "$SCRIPT_DIR/.skinner/skinner.sh" "$@"
