(ns status-im.navigation.roots
  (:require
    [quo.foundations.colors :as colors]
    [status-im.constants :as constants]
    [status-im.navigation.options :as options]))

;; Theme Order for navigation roots
;; 1. Themes hardcoded in below map
;; 2. If nil or no entry in map, then theme stored in
;;    [:db :profile/profile :appearance] will be used (for mulitaccounts)
;; 3). Fallback theme - Dark
(def themes
  {:intro       constants/theme-type-dark
   :profiles    constants/theme-type-dark
   :shell-stack nil})

(defn roots-internal
  []
  {:intro
   {:root
    {:stack {:id       :intro
             :children [{:component {:name    :intro
                                     :id      :intro
                                     :options (options/dark-root-options)}}]}}}
   :shell-stack
   {:root
    {:stack {:id       :shell-stack
             :children [{:component {:name    :shell-stack
                                     :id      :shell-stack
                                     :options (options/root-options
                                               {:nav-bar-color colors/neutral-100})}}]}}}
   :profiles
   {:root
    {:stack {:id       :profiles
             :children [{:component {:name    :profiles
                                     :id      :profiles
                                     :options (options/dark-root-options)}}]}}}

   :enable-notifications
   {:root {:stack {:children [{:component {:name    :enable-notifications
                                           :id      :enable-notifications
                                           :options (options/dark-root-options)}}]}}}

   :welcome
   {:root {:stack {:children [{:component {:name    :welcome
                                           :id      :welcome
                                           :options (options/dark-root-options)}}]}}}
   :syncing-results
   {:root {:stack {:children [{:component {:name    :syncing-results
                                           :id      :syncing-results
                                           :options (options/dark-root-options)}}]}}}})

(defn old-roots
  []
  {:progress
   {:root {:stack {:children [{:component {:name    :progress
                                           :id      :progress
                                           :options (options/root-options nil)}}]
                   :options  (assoc (options/root-options nil)
                                    :topBar
                                    {:visible false})}}}})

(defn roots
  []
  (merge
   (old-roots)
   (roots-internal)))
