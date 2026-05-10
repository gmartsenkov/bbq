# bash completion for httpee.
#
# Assumes httpee is on $PATH (via `bbin install ~/Development/http-bb`).
#
# Install:
#   echo "source ~/Development/http-bb/completions/httpee.bash" >> ~/.bashrc
#   exec bash   # reload

_httpee_complete() {
  local cur="${COMP_WORDS[COMP_CWORD]}"
  COMPREPLY=( $(compgen -W "$(httpee --complete 2>/dev/null)" -- "$cur") )
}

complete -F _httpee_complete httpee
