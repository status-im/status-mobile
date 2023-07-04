(ns status-im2.navigation.roots
  (:require [status-im2.navigation.view :as views]
            [quo2.foundations.colors :as colors]
            [status-im2.navigation.options :as options]
            [status-im2.constants :as constants]))

(defn get-screen-options
  [screen]
  (merge (get-in views/screens [screen :options])
         (options/statusbar-and-navbar-root)
         (options/merge-top-bar (options/topbar-options)
                                (get-in views/screens [screen :options]))))

(defn old-roots
  []
  {;; ONBOARDING
   :onboarding
   {:root {:stack {:id       :onboarding
                   :children [{:component {:name    :get-your-keys
                                           :id      :get-your-keys
                                           :options (options/statusbar-and-navbar-root)}}]
                   :options  (merge (options/default-root)
                                    (options/statusbar-and-navbar-root)
                                    {:topBar (assoc (options/topbar-options)
                                                    :elevation 0
                                                    :noBorder  true
                                                    :animate   false)})}}}

   ;;PROGRESS
   :progress
   {:root {:stack {:children [{:component {:name    :progress
                                           :id      :progress
                                           :options (options/statusbar-and-navbar-root)}}]
                   :options  (merge (options/default-root)
                                    {:topBar (assoc (options/topbar-options) :visible false)})}}}

   ;;LOGIN
   :multiaccounts
   {:root {:stack {:id       :multiaccounts-stack
                   :children [{:component {:name    :multiaccounts
                                           :id      :multiaccounts
                                           :options (get-screen-options :multiaccounts)}}
                              {:component {:name    :login
                                           :id      :login
                                           :options (get-screen-options :login)}}]
                   :options  (merge (options/default-root)
                                    (options/statusbar-and-navbar-root)
                                    {:topBar (options/topbar-options)})}}}

   :multiaccounts-keycard
   {:root {:stack {:id       :multiaccounts-stack
                   :children [{:component {:name    :profiles
                                           :id      :profiles
                                           :options (get-screen-options :multiaccounts)}}
                              {:component {:name    :keycard-login-pin
                                           :id      :keycard-login-pin
                                           :options (get-screen-options :keycard-login-pin)}}]
                   :options  (merge (options/default-root)
                                    (options/statusbar-and-navbar-root)
                                    {:topBar (options/topbar-options)})}}}

   ;;NOTIFICATIONS
   :onboarding-notification
   {:root {:stack {:children [{:component {:name    :onboarding-notification
                                           :id      :onboarding-notification
                                           :options (options/statusbar-and-navbar-root)}}]
                   :options  (merge (options/default-root)
                                    (options/statusbar-and-navbar-root)
                                    {:topBar (assoc (options/topbar-options) :visible false)})}}}

   ;; TERMS OF SERVICE
   :tos
   {:root {:stack {:children [{:component {:name    :force-accept-tos
                                           :id      :force-accept-tos
                                           :options (get-screen-options :force-accept-tos)}}]
                   :options  (merge (options/default-root)
                                    (options/statusbar-and-navbar-root)
                                    {:topBar (assoc (options/topbar-options) :visible false)})}}}})

;; Theme Order for navigation roots
;; 1. Themes hardcoded in below map
;; 2. If nil or no entry in map, then theme stored in
;;    [:db :profile/profile :appearance] will be used (for mulitaccounts)
;; 3). Fallback theme - Dark
(def themes
  {:intro       constants/theme-type-dark
   :profiles    constants/theme-type-dark
   :shell-stack nil})

(defn roots
  []
  (merge (old-roots)

         {:intro
          {:root
           {:stack {:id       :intro
                    :children [{:component {:name    :intro
                                            :id      :intro
                                            :options (options/default-root nil colors/neutral-100)}}]}}}
          :shell-stack
          {:root
           {:stack {:id       :shell-stack
                    :children [{:component {:name    :shell-stack
                                            :id      :shell-stack
                                            :options (options/default-root
                                                      (if (colors/dark?) :light :dark))}}]}}}
          :profiles
          {:root
           {:stack {:id       :profiles
                    :children [{:component {:name    :profiles
                                            :id      :profiles
                                            :options (options/default-root)}}]}}}

          :enable-notifications
          {:root {:stack {:children [{:component {:name    :enable-notifications
                                                  :id      :enable-notifications
                                                  :options (options/default-root)}}]}}}

          :welcome
          {:root {:stack {:children [{:component {:name    :welcome
                                                  :id      :welcome
                                                  :options (options/default-root)}}]}}}
          :syncing-results
          {:root {:stack {:children [{:component {:name    :syncing-results
                                                  :id      :syncing-results
                                                  :options (options/default-root)}}]}}}}))
