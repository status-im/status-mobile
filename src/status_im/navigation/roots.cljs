(ns status-im.navigation.roots
  (:require
    [quo.foundations.colors :as colors]
    [status-im.navigation.options :as options]))

(defn roots-internal
  [status-bar-theme]
  {:screen/onboarding.intro
   {:root
    {:stack {:id       :screen/onboarding.intro
             :children [{:component {:name    :screen/onboarding.intro
                                     :id      :screen/onboarding.intro
                                     :options (options/dark-root-options)}}]}}}
   :shell-stack
   {:root
    {:stack {:id       :shell-stack
             :children [{:component {:name    :shell-stack
                                     :id      :shell-stack
                                     :options (options/root-options
                                               {:nav-bar-color    colors/neutral-100
                                                :status-bar-theme status-bar-theme})}}]}}}
   :screen/profile.profiles
   {:root
    {:stack {:id       :screen/profile.profiles
             :children [{:component {:name    :screen/profile.profiles
                                     :id      :screen/profile.profiles
                                     :options (options/dark-root-options)}}]}}}

   :screen/onboarding.syncing-biometric
   {:root {:stack {:id       :screen/onboarding.enable-biometrics
                   :children [{:component {:name    :screen/onboarding.enable-biometrics
                                           :id      :screen/onboarding.enable-biometrics
                                           :options (options/dark-root-options)}}]}}}})

(defn old-roots
  [status-bar-theme]
  {:progress
   {:root {:stack {:children [{:component {:name    :progress
                                           :id      :progress
                                           :options (options/root-options {:status-bar-theme
                                                                           status-bar-theme})}}]
                   :options  (assoc (options/root-options nil)
                                    :topBar
                                    {:visible false})}}}})

(defn roots
  [status-bar-theme]
  (merge
   (old-roots status-bar-theme)
   (roots-internal status-bar-theme)))
