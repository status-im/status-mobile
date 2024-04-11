(ns utils.worklets.profile-header)

(def ^:private worklets (js/require "../src/js/worklets/profile_header.js"))

(defn profile-header-animation
  [scroll-y threshold top-bar-height]
  (.profileHeaderAnimation ^js worklets
                           scroll-y
                           threshold
                           top-bar-height))
