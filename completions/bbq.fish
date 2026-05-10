# fish completion for bbq.
#
# Assumes bbq is on $PATH (via `bbin install ~/Development/http-bb`).
#
# Install:
#   source ~/Development/http-bb/completions/bbq.fish

complete -c bbq -f -a "(bbq --complete 2>/dev/null)"
