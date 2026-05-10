# zsh completion for httpee.
#
# Assumes httpee is on $PATH (via `bbin install ~/Development/http-bb`).
#
# Install:
#   echo "source ~/Development/http-bb/completions/httpee.zsh" >> ~/.zshrc
#   exec zsh   # reload

_httpee_complete() {
  local -a templates
  templates=( ${(@f)"$(httpee --complete 2>/dev/null)"} )
  _describe 'template' templates
}

compdef _httpee_complete httpee
