(ns status-im.navigation.roots
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.screens.views :as views]))

(defn status-bar-options []
  (if platform/android?
    {:navigationBar {:backgroundColor colors/white}
     :statusBar     {:backgroundColor colors/white
                     :style           (if (colors/dark?) :light :dark)}}
    {:statusBar {:style (if (colors/dark?) :light :dark)}}))

(defn topbar-options []
  {:noBorder   true
   :elevation  0
   :title {:color colors/black}
   :rightButtonColor colors/black
   :background {:color colors/white}
   :backButton {:icon  (icons/icon-source :main-icons/arrow-left)
                :color colors/black}})

(defn bottom-tab-general [icon accessibility]
  (merge
   {:testID accessibility
    :accessibilityLabel accessibility
    :fontSize  11
    :icon (icons/icon-source icon)
    :badgeColor colors/blue
    :dotIndicator {:color colors/blue :visible false :size 10}
    :iconColor colors/gray :selectedIconColor colors/blue
    :textColor colors/gray :selectedTextColor colors/blue}
   (when platform/android?
     {:badge ""})))

(defn default-root []
  {:layout {:componentBackgroundColor colors/white
            :backgroundColor          colors/white}})

(defn merge-top-bar [root-options options]
  (let [options (:topBar options)]
    {:topBar
     (merge root-options
            options
            (when (or (:title root-options) (:title options))
              {:title (merge (:title root-options) (:title options))})
            (when (or (:background root-options) (:background options))
              {:background (merge (:background root-options) (:background options))})
            (when (or (:backButton root-options) (:backButton options))
              {:backButton (merge (:backButton root-options) (:backButton options))})
            (when (or (:leftButtons root-options) (:leftButtons options))
              {:leftButtons (merge (:leftButtons root-options) (:leftButtons options))})
            (when (or (:rightButtons root-options) (:rightButtons options))
              {:rightButtons (merge (:rightButtons root-options) (:rightButtons options))}))}))

(defn get-screen-options [screen]
  (merge (get-in views/screens [screen :options])
         (status-bar-options)
         (merge-top-bar (topbar-options)
                        (get-in views/screens [screen :options]))))

;;TODO problem here is that we have two places for screens, here and in screens ns, and we have handler in navigate
(defn roots []
  ;;TABS
  {:chat-stack
   {:root
    {:bottomTabs
     {:id       :tabs-stack
      :options  (merge (default-root)
                       {:bottomTabs {:titleDisplayMode :alwaysHide
                                     :backgroundColor  colors/white}})
      :children [;CHAT STACK
                 {:stack {:id       :chat-stack
                          :children [{:component {:name    :home
                                                  :id      :home
                                                  :options (merge (status-bar-options)
                                                                  {:topBar (assoc (topbar-options) :visible false)})}}]
                          :options  {:bottomTab (bottom-tab-general :main-icons/message :home-tab-button)}}}
                 ;BROWSER STACK
                 {:stack {:id       :browser-stack
                          :children [{:component {:name    :empty-tab
                                                  :id      :empty-tab
                                                  :options (merge (status-bar-options)
                                                                  {:topBar (assoc (topbar-options) :visible false)})}}]

                          :options  {:bottomTab (bottom-tab-general :main-icons/browser :dapp-tab-button)}}}
                 ;WALLET STACK
                 {:stack {:id       :wallet-stack
                          :children [{:component {:name    :wallet
                                                  :id      :wallet
                                                  :options (merge (status-bar-options)
                                                                  {:topBar (assoc (topbar-options) :visible false)})}}]
                          :options  {:bottomTab (bottom-tab-general :main-icons/wallet :wallet-tab-button)}}}
                 ;STATUS STACK
                 {:stack {:id       :status-stack
                          :children [{:component {:name    :status
                                                  :id      :status
                                                  :options (merge (status-bar-options)
                                                                  {:topBar (assoc (topbar-options) :visible false)})}}]
                          :options  {:bottomTab (bottom-tab-general :main-icons/status :status-tab-button)}}}
                 ;PROFILE STACK
                 {:stack {:id       :profile-stack
                          :children [{:component {:name    :my-profile
                                                  :id      :my-profile
                                                  :options (merge (status-bar-options)
                                                                  {:topBar (assoc (topbar-options) :visible false)})}}]
                          :options  {:bottomTab (bottom-tab-general :main-icons/user-profile :profile-tab-button)}}}]}}}

   ;;INTRO (onboarding carousel)
   :intro
   {:root {:stack {:children [{:component {:name    :intro
                                           :id      :intro
                                           :options (status-bar-options)}}]
                   :options  (merge (default-root)
                                    {:topBar (assoc (topbar-options) :visible false)})}}}

   ;; ONBOARDING
   :onboarding
   {:root {:stack {:id       :onboarding
                   :children [{:component {:name    :get-your-keys
                                           :id      :get-your-keys
                                           :options (status-bar-options)}}]
                   :options  (merge (default-root)
                                    {:topBar (assoc (topbar-options) :elevation 0 :noBorder true :animate false)})}}}

   ;;PROGRESS
   :progress
   {:root {:stack {:children [{:component {:name    :progress
                                           :id      :progress
                                           :options (status-bar-options)}}]
                   :options  (merge (default-root)
                                    {:topBar (assoc (topbar-options) :visible false)})}}}

   ;;LOGIN
   :multiaccounts
   {:root {:stack {:id       :multiaccounts-stack
                   :children [{:component {:name    :multiaccounts
                                           :id      :multiaccounts
                                           :options (get-screen-options :multiaccounts)}}
                              {:component {:name    :login
                                           :id      :login
                                           :options (get-screen-options :login)}}]
                   :options  (merge (default-root)
                                    {:topBar (topbar-options)})}}}

   :multiaccounts-keycard
   {:root {:stack {:id       :multiaccounts-stack
                   :children [{:component {:name    :multiaccounts
                                           :id      :multiaccounts
                                           :options (get-screen-options :multiaccounts)}}
                              {:component {:name    :keycard-login-pin
                                           :id      :keycard-login-pin
                                           :options (get-screen-options :keycard-login-pin)}}]
                   :options  (merge (default-root)
                                    {:topBar (topbar-options)})}}}

   ;;WELCOME
   :welcome
   {:root {:stack {:children [{:component {:name    :welcome
                                           :id      :welcome
                                           :options (status-bar-options)}}]
                   :options  (merge (default-root)
                                    {:topBar (assoc (topbar-options) :visible false)})}}}

   ;;NOTIFICATIONS
   :onboarding-notification
   {:root {:stack {:children [{:component {:name    :onboarding-notification
                                           :id      :onboarding-notification
                                           :options (status-bar-options)}}]
                   :options  (merge (default-root)
                                    {:topBar (assoc (topbar-options) :visible false)})}}}})