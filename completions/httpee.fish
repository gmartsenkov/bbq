# fish completion for httpee.
#
# Assumes httpee is on $PATH (via `bbin install ~/Development/http-bb`).
#
# Install:
#   source ~/Development/http-bb/completions/httpee.fish

complete -c httpee -f -a "(httpee --complete 2>/dev/null)"
