(ns status-im.contexts.browser.footer.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im.contexts.browser.components.dapp-bar :as dapp-bar]
            [status-im.contexts.browser.components.dapp-icon :as dapp-icon]
            [status-im.contexts.browser.constants :as browser.constants]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.re-frame :as rf]))

(defn dapp-bar
  []
  (let [tab-id (rf/sub [:browser/focused-tab-id])
        dapp   (rf/sub [:browser/dapp-for-tab tab-id])]
    [dapp-bar/container
     [dapp-icon/view
      {:dapp dapp
       :size 24}]
     [dapp-bar/title {:text (:title dapp)}]
     [dapp-bar/info-button]]))

(defn profile-btn
  []
  (let [{:keys [public-key] :as profile} (rf/sub [:profile/profile-with-image])
        online?                          (rf/sub [:visibility-status-updates/online?
                                                  public-key])
        customization-color              (rf/sub [:profile/customization-color])
        avatar-props                     {:online?         online?
                                          :full-name       (profile.utils/displayed-name profile)
                                          :profile-picture (profile.utils/photo profile)}
        on-press                         #(rf/dispatch [:open-modal :screen/settings])]
    [rn/pressable
     {:on-press            on-press
      :style               {:padding          4
                            :background-color colors/neutral-80
                            :border-radius    40}
      :accessibility-label :open-profile}
     [quo/user-avatar
      (merge {:status-indicator?   true
              :customization-color customization-color
              :size                :small}
             avatar-props)]]))

(defn on-tabs-press
  [browser-mode]
  (condp = browser-mode
    :browser-mode/browser (rf/dispatch [:browser/show-tabs])
    :browser-mode/tabs    (rf/dispatch [:browser/show-browser])
    nil))

(defn tabs-btn-icon
  [browser-mode]
  (condp = browser-mode
    :browser-mode/tabs    :i/browser
    :browser-mode/browser :i/tabs
    :i/tabs))

(defn tabs-btn
  []
  (let [browser-mode (rf/sub [:browser/mode])]
    [rn/pressable
     {:style    {:flex            1
                 :align-items     :center
                 :justify-content :center}
      :on-press #(on-tabs-press browser-mode)}
     [rn/view {:style {:transform [{:scale 1.2}]}}
      [quo/icon (tabs-btn-icon browser-mode) {:size 20 :color colors/white}]]]))

(defn view
  []
  [reanimated/view
   {:style {:width              browser.constants/browser-width
            :background-color   colors/neutral-100
            :align-items        :center
            :justify-content    :space-between
            :flex-direction     :row
            :padding-horizontal 20
            :height             browser.constants/footer-height}}
   [rn/view
    {:style {:align-items     :center
             :justify-content :center
             :margin-right    8}}
    [profile-btn]]
   [dapp-bar]
   [rn/view
    {:style {:width            44
             :height           44
             :margin-left      8
             :background-color colors/neutral-80
             :border-radius    12}}
    [tabs-btn]]])
