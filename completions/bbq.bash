# bash completion for bbq.
#
# Assumes bbq is on $PATH (via `bbin install ~/Development/http-bb`).
#
# Install:
#   echo "source ~/Development/http-bb/completions/bbq.bash" >> ~/.bashrc
#   exec bash   # reload

_bbq_complete() {
  local cur="${COMP_WORDS[COMP_CWORD]}"
  COMPREPLY=( $(compgen -W "$(bbq --complete 2>/dev/null)" -- "$cur") )
}

complete -F _bbq_complete bbq
