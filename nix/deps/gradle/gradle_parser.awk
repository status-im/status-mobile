# This script parses the AWFUL Gradle output of :dependencies calls
# and outputs just the names of the packages in the Maven format:
# <groupId>:<artifactId>:<version>

function findPackage(line, regex) {
    rval = match(line, regex, matches)
    if (rval != 0) {
        dep = sprintf("%s:%s:%s", matches[1], matches[2], matches[3])
        if (deps[dep] == nil) {
            deps[dep] = 1
        }
        return 1
    }
    return 0
}

# Gradle outputs dependencies in groups defined by configurations.
# Those configurations are words followed by a dash and a description.
# There's also a special 'classpath' configuration we want.
/^(classpath|[a-zA-Z0-9]+)( - .*)?$/ {
    # Ignore configurations starting with 'test'
    if (tolower($1) ~ /^test/) {
        next
    }

    # Lines after configuration name list packages
    for (getline line; line != ""; getline line) {

        # JavaScript Core (JSC) is provided by node_modules
        if (line ~ "org.webkit:android-jsc") { continue }

        # Example: +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.50
        if (findPackage(line, "--- ([^ :]+):([^ :]+):([^ :]+)$")) {
            continue
        }

        # Example: +--- androidx.lifecycle:lifecycle-common:{strictly 2.0.0} -> 2.0.0 (c)
        if (findPackage(line, "--- ([^ :]+):([^ :]+):[^:]+ -> ([^ :]+) ?(\\([*c]\\))?$")) {
            continue
        }

        # Example: +--- com.android.support:appcompat-v7:28.0.0 -> androidx.appcompat:appcompat:1.0.2
        if (findPackage(line, "--- [^ :]+:[^ :]+:[^ ]+ -> ([^ :]+):([^ :]+):([^ :]+)$")) {
            continue
        }
    }
}

END{
    # It's nicer to sort it
    asorti(deps, sorted)
    for (i in sorted) {
        print sorted[i]
    }
}
