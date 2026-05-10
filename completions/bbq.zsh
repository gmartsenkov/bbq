# zsh completion for bbq.
#
# Assumes bbq is on $PATH (via `bbin install ~/Development/http-bb`).
#
# Install:
#   echo "source ~/Development/http-bb/completions/bbq.zsh" >> ~/.zshrc
#   exec zsh   # reload

_bbq_complete() {
  local -a templates
  templates=( ${(@f)"$(bbq --complete 2>/dev/null)"} )
  _describe 'template' templates
}

compdef _bbq_complete bbq
