#!/usr/bin/env python3
import json
import sys
from getpass import getpass
from argparse import ArgumentParser
from calendar import monthrange
from datetime import datetime, timedelta
from operator import itemgetter

try:
    import requests
except ImportError as e:
    print('Please have {} installed before running this script.'.format(e.name))
    sys.exit(-1)

def parse_cmdargs():
    '''
    Creates and parses command-line arguments.
    '''
    deltas = ['day', 'week', 'month', 'year']
    parser = ArgumentParser(
        description='Outputs CSV to STDOUT that tracks the users that place `bounty-awaiting-approval` label onto issues, given a repository or an organization.')
    place_group = parser.add_mutually_exclusive_group(required=True)
    place_group.add_argument('-r', '--repo', help='Repository to search', type=str)
    place_group.add_argument('-o', '--organization', help='Organization to search', type=str)
    parser.add_argument('user', help='Github handle', type=str)
    parser.add_argument('-d', '--delta', help='Time period (default: week)', default='week', choices=deltas)
    parser.add_argument('-v', '--verbose', action='store_true')
    return parser.parse_args()

def timedate_to_interval(td, delta):
    '''
    Converts a timedate into the time period (string) that it belongs to (i.e.
    encloses it). Length of time period depends on `delta`, which could either
    be 'week', 'month', 'year'. If none apply, it returns the timedate in ISO
    format. Returns said time period otherwise.
    '''
    if delta == 'week':
        _, _, week_day = td.isocalendar()
        starting_date = td + timedelta(1 - week_day)
        ending_date = td + timedelta(7 - week_day)
        return '{}-{}'.format(starting_date.date().isoformat(),
                              ending_date.date().isoformat())
    elif delta == 'month':
        _, days_in_month = monthrange(td.year, td.month)
        return '{0}-{1}-01-{0}-{1}-{2}'.format(td.year, td.month, days_in_month)
    elif delta == 'year':
        return '{0}-01-01-{0}-12-31'.format(td.year)
    else:
        # Return day format otherwise
        return td.date().isoformat()

def get_events(auth, repo, label):
    '''
    Given a label, finds all issues with said label, and returns a list of urls
    leading to the events.
    '''
    issues_base = create_issues_url(repo, label)
    index = 1
    events = []
    while True:
        # Gets them persistantly, until no more exist
        r = requests.get(issues_base + str(index), auth=auth)

        if r.status_code != 200:
            print('error: ' + r.content.decode('utf-8'))
            break

        issues = list(filter(lambda i: 'pull_request' not in i, json.loads(r.content)))
        index += 1

        if len(issues) == 0:
            break

        events.extend(list(map(itemgetter('events_url'), issues)))

    return events

def get_bounty_placers(auth, event_url, delta):
    '''
    Given the API URL that returns a specific GH issue's events, returns the
    timestamp and handle that added 'bounty-awaiting-approval' label onto issue,
    or None if not found. `delta` refers to the length of the period, either
    {day, week, month, year}.
    '''
    bounty_indicators = ['bounty-awaiting-approval', 'bounty']
    index = 1
    while True:
        r = requests.get(event_url + '?page=%d' % index, auth=auth)

        if r.status_code != 200:
            print('error: ' + r.content.decode('utf-8'))
            break

        issue_evts = json.loads(r.content)

        # Skip this list of events that doesn't exist
        if not issue_evts:
            break

        for ie in issue_evts:
            if ie['event'] == 'labeled' and ie['label']['name'] in bounty_indicators:
                d = datetime.strptime(ie['created_at'], '%Y-%m-%dT%H:%M:%SZ')
                handle = ie['actor']['login']
                return [timedate_to_interval(d, delta), handle]

        index += 1
    return None

def create_issues_url(repo, label):
    '''
    Creates a formatted URL that returns issues, given the repo and label. URL
    is returned.
    '''
    base_url = 'https://api.github.com/repos'
    query = 'issues?labels={}&per_page=100&page='.format(label)
    return '{}/{}/{}'.format(base_url, repo, query)

def get_repos_from_org(auth, org):
    '''
    Returns a list of repositories from an organization.
    '''
    base_url = 'https://api.github.com/orgs'
    query = '{}/{}/repos?per_page=100&page='.format(base_url, org)
    repos = []
    index = 1
    while True:
        # Gets them persistantly, until no more exist
        r = requests.get(query + str(index), auth=auth)

        if r.status_code != 200:
            print('error: ' + r.content.decode('utf-8'))
            break

        gotten_repos = list(map(itemgetter('full_name'), json.loads(r.content)))
        index += 1

        if not gotten_repos:
            break

        repos.extend(gotten_repos)

    return repos

def main():
    args = parse_cmdargs()
    auth = (args.user, getpass(prompt='Password/Access Token:'))
    delta = args.delta
    repos = []
    events = []

    # Populate repositories to be searched, whether it be 1 or dozens!
    if args.repo:
        repos.append(args.repo)
    elif args.organization:
        repos = get_repos_from_org(auth, args.organization)
        if args.verbose:
            print('Got {} repositories.'.format(len(repos)), file=sys.stderr)

    for i, repo in enumerate(repos, 1):
        # Get all the issues from a repository, and grab all corresponding event urls
        if args.verbose:
            print('({}/{}) Getting events from "{}"...'.format(i, len(repos), repo), file=sys.stderr)
        events += get_events(auth, repo, 'bounty')
        events += get_events(auth, repo, 'bounty-awaiting-approval')

    # Tries to find the event where a user labels the issue with
    # 'bounty-awaiting-approval', and adds the corresponding time interval, along
    # with the Github handle, into a list.
    entries = [get_bounty_placers(auth, e, delta) for e in events]

    # Aggregate all collected entries under a time interval, separated by Github
    # handle.
    intervals = {}
    for interval, handle in entries:
        if interval not in intervals:
            intervals[interval] = {handle: 1}
        elif handle not in intervals[interval]:
            intervals[interval][handle] = 1
        else:
            intervals[interval][handle] += 1

    print('PERIOD,GITHUB_NAME,BOUNTIES_POSTED')
    for interval, handles in intervals.items():
        for handle, freq in handles.items():
            print('{},{},{}'.format(interval, handle, freq))

if __name__ == '__main__':
    main()
