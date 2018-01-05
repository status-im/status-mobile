#!/usr/bin/env bash

# Will scan a github repo to compute some statistics on PR
# requires curl and bc (presents on most unix like system)


# PARAMETERS ----------------------------
# Github repo to scan
GITHUB_REPO="status-im/status-react"

# Computational mode
# available values :
# open : catch only open PRs, not yet merged nor closed and then compute five number
#        summary on opened time until today (open is default)
# merge : catch only merged AND closed PRs and then compute five number summary
#         on time spent before PR was merged
MODE=open

# Filter PR by date
#   This parameter is optional, you could empty these values to disable filter by date
#   Will only catch PR created between these dates
#   if END_TIME is empty, it will assume today as value
# WARN : PR github api limit each result to a 30 page size
#       So depending on chosen date, it could be a lot of requests
# available values : YYYY-MM-DD
START_TIME="2018-01-01"
END_TIME="2018-02-01"

# Verbose mode
#   print more info (0 is default)
# available values : 0 | 1
VERBOSE=1


# INTERNAL PARAMETERS ----------------------------
# Determine platform
case "$OSTYPE" in
  darwin*)  OS="DARWIN" ;;
  *)        OS="UNIX" ;;
esac

# Check available tools
# curl and bc (which are present on most unix platform)
type curl &>/dev/null || _err=1
if [ "$_err" ]; then
    echo "ERROR : you need curl on this platform"
    exit 1
fi
type bc &>/dev/null || _err=1
if [ "$_err" ]; then
    echo "ERROR : you need bc on this platform"
    exit 1
fi

# Fix date filter
NOW="$(date +%s)"
FILTER_TEXT=""
if [ ! "$START_TIME" = "" ]; then
  FILTER="ON"
  FILTER_TEXT=" created between $START_TIME"
  [ "$OS" = "DARWIN" ] && START_TIME="$(date -j -u -f '%Y-%m-%d' "$START_TIME" +%s)" \
                          || START_TIME="$(date -d "$START_TIME" +%s)"

  # we get today by default for END_TIME
  if [ "$END_TIME" = "" ]; then
    FILTER_TEXT="$FILTER_TEXT and today"
    END_TIME="$NOW"
  else
    FILTER_TEXT="$FILTER_TEXT and $END_TIME"
    [ "$OS" = "DARWIN" ] && END_TIME="$(date -j -u -f '%Y-%m-%d' "$END_TIME" +%s)" \
                            || END_TIME="$(date -d "$END_TIME" +%s)"
  fi
fi

# FUNCTIONS ---------------------------
usage() {
  echo "USAGE"
  echo "$0 -h : get help"
  echo "$0 [merge|open] : compute stats on merged PR or opened PR (open is default)"
}

github_pr_merge() {
  repo="$1"
  list_spent_time=""
  list_dt_closed=""
  list_dt_created=""

	last_page=$(curl -i -sL "https://api.github.com/repos/$repo/pulls?sort=created&state=closed" | grep rel=\"last\" | cut -d "," -f 2 | cut -d "=" -f 4 | cut -d ">" -f 1)
  [ "$last_page" = "" ] && last_page=1
  [ "$VERBOSE" = "1" ] && echo "" >&2
  for i in $(seq 1 $last_page); do
    [ "$VERBOSE" = "1" ] && tput cuu 1 >&2 && tput el >&2 && echo "[Analysing PRs list: $i/$last_page]" >&2
    json="$(curl -sL "https://api.github.com/repos/$repo/pulls?sort=created&direction=desc&state=closed&page=$i")"

    # get PR merged AND closed - we use closed date to compute stat and we ignore if its merged date is null
    list_tmp="$(echo "$json" | grep -A1 -B2 "closed_at" | grep --invert-match null | grep -B4 "merged_at" | grep -A3 "created_at")"
    list_dt_created="$list_dt_created $(echo "$list_tmp" | grep "created_at" | tr -d ' ' | cut -d "\"" -f 4)"
    list_dt_closed="$list_dt_closed $(echo "$list_tmp" | grep "closed_at" | tr -d ' ' | cut -d "\"" -f 4)"

    len_list_dt="$(echo $list_dt_created | wc -w)"

    for k in $(seq 1 $len_list_dt); do
      dt_crea="$(echo $list_dt_created | cut -d " " -f $k)"
      [ "$OS" = "DARWIN" ] && dt_crea="$(date -j -u -f '%Y-%m-%dT%H:%M:%SZ' "$dt_crea" +%s)" \
        || dt_crea="$(date -d "$dt_crea" +%s)"

      dt_closed="$(echo $list_dt_closed | cut -d " " -f $k)"
      [ "$OS" = "DARWIN" ] && dt_closed="$(date -j -u -f '%Y-%m-%dT%H:%M:%SZ' "$dt_closed" +%s)" \
        || dt_closed="$(date -d "$dt_closed" +%s)"

      if [ "$FILTER" = "ON" ]; then
        if [ "1" = "$(echo "$START_TIME <= $dt_crea" | bc -l)" ]; then
          if [ "1" = "$(echo "$END_TIME >= $dt_crea" | bc -l)" ]; then
            (( spent_time = dt_closed - dt_crea ))
            list_spent_time="$list_spent_time $spent_time"
          fi
        fi
      else
        (( spent_time = dt_closed - dt_crea ))
        list_spent_time="$list_spent_time $spent_time"
      fi
    done
    if [ "$FILTER" = "ON" ]; then
      # we do not catch anymore page because we get all PR for the period
      if [ "1" = "$(echo "$START_TIME > $dt_crea" | bc -l)" ]; then
        [ "$VERBOSE" = "1" ] && echo "[Stop fetching PR, other PR are out of the selected time]" >&2
        break
      fi
    fi
  done

  echo "$list_spent_time" | tr ' ' '\n' | sort -n | tr ' ' '\n'
}

github_pr_open() {
  repo="$1"
  list_spent_time=""
  list_dt_created=""

	last_page=$(curl -i -sL "https://api.github.com/repos/$repo/pulls?sort=created&state=open" | grep rel=\"last\" | cut -d "," -f 2 | cut -d "=" -f 4 | cut -d ">" -f 1)
  [ "$last_page" = "" ] && last_page=1
  [ "$VERBOSE" = "1" ] && echo "" >&2
  for i in $(seq 1 $last_page); do
    [ "$VERBOSE" = "1" ] && tput cuu 1 >&2 && tput el >&2 && echo "[Analysing PRs list: $i/$last_page]" >&2
    json="$(curl -sL "https://api.github.com/repos/$repo/pulls?sort=created&direction=desc&state=open&page=$i")"
		list_dt_created="$list_dt_created $(echo "$json" | grep -B2 "closed_at" | grep "created_at" | tr -d ' ' | cut -d "\"" -f 4)"

    for dt_crea in $list_dt_created; do
      [ "$OS" = "DARWIN" ] && dt_crea="$(date -j -u -f '%Y-%m-%dT%H:%M:%SZ' "$dt_crea" +%s)" \
                            || dt_crea="$(date -d "$dt_crea" +%s)"

      if [ "$FILTER" = "ON" ]; then
        if [ "1" = "$(echo "$START_TIME <= $dt_crea" | bc -l)" ]; then
          if [ "1" = "$(echo "$END_TIME >= $dt_crea" | bc -l)" ]; then
            (( spent_time = NOW - dt_crea ))
            list_spent_time="$list_spent_time $spent_time"
          fi
        fi
      else
        (( spent_time = NOW - dt_crea ))
        list_spent_time="$list_spent_time $spent_time"
      fi
    done
    if [ "$FILTER" = "ON" ]; then
      # we do not catch anymore page because we get all PR for the period
      if [ "1" = "$(echo "$START_TIME > $dt_crea" | bc -l)" ]; then
        [ "$VERBOSE" = "1" ] && echo "[Stop fetching PRs, next will be out of the selected time]" >&2
        break
      fi
    fi

  done

  echo "$list_spent_time" | tr ' ' '\n' | sort -n | tr ' ' '\n'
}


median_list() {
  list="$@"
  len_list="$#"
  median=""

  if [ $len_list -eq 1 ]; then
    middle_list=1
    middle_list_next=1
  else
    (( middle_list = len_list / 2 ))
    (( middle_list_next = middle_list + 1 ))
  fi

  j=1
  for l in $list; do
    [ $j -eq $middle_list ] && median=$l
    if [ $j -eq $middle_list_next ]; then
      median=$(echo "scale=2; ($median + $l)/2" | bc -l)
      break
    fi
    (( j = j + 1 ))
  done
  echo "$median"
}

convert_sec() {
  secs="$1"
  printf '%dd %02dh:%02dm:%02ds\n' $(($secs/86400)) $(($secs%86400/3600)) $(($secs%3600/60)) $(($secs%60))
}

five_nb_summary() {
  list="$@"
  len_list="$#"
  sample_minimum=""
  lower_quartile=""
  median=""
  upper_quartile=""
  sample_maximum=""


  if [ $len_list -gt 0 ]; then
    # NOTE list is already sorted asc
    sample_minimum=$(echo $list | cut -d " " -f 1)
    sample_maximum=$(echo $list | cut -d " " -f $len_list)

    median="$(median_list $list)"
    for l in $list; do
      # Q1
      [ "1" = "$(echo "$l <= $median" | bc -l)" ] && q1_list="$q1_list $l"
      # Q3
      [ "1" = "$(echo "$l >= $median" | bc -l)" ] && q3_list="$q3_list $l"
    done

    lower_quartile="$(median_list $q1_list | cut -d '.' -f 1)"
    median="$(echo $median | cut -d '.' -f 1)"
    upper_quartile="$(median_list $q3_list | cut -d '.' -f 1)"
  fi

  [ "$VERBOSE" = "1" ] && echo "[Stats computed on $len_list PR$FILTER_TEXT]" >&2
  printf "\n"
  printf "%16s | %16s | %16s | %16s | %16s" "Min" "Q1" "Mean" "Q3" "Max"
  printf "\n"
  [ $len_list -gt 0 ] && printf "%16s | %16s | %16s | %16s | %16s" "$(convert_sec "$sample_minimum")" "$(convert_sec "$lower_quartile")" "$(convert_sec "$median")" "$(convert_sec "$upper_quartile")" "$(convert_sec "$sample_maximum")"
  printf "\n"
}


# MAIN ----------------------------

if [ "$1" = "-h" ]; then
  usage
  exit
fi

case $1 in
  merge|open )
    MODE=$1
    ;;
esac

if [ "$VERBOSE" = "1" ]; then
  echo "[Fetching $MODE PRs stats from $GITHUB_REPO]" >&2
fi

list_time="$(github_pr_$MODE $GITHUB_REPO)"
five_nb_summary $list_time
