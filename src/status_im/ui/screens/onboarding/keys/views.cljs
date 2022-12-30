(ns status-im.ui.screens.onboarding.keys.views
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.onboarding.styles :as styles]
            [status-im.ui.screens.onboarding.views :as ui]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.utils :as utils]
            [utils.debounce :refer [dispatch-and-chill]])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview choose-a-chat-name
  []
  (letsubs [{:keys [multiaccounts selected-id]} [:intro-wizard/choose-key]]
    [:<>
     [topbar/topbar
      {:border-bottom false
       :navigation    {:label    (i18n/label :t/cancel)
                       :on-press (fn []
                                   (utils/show-question
                                    (i18n/label :t/are-you-sure-to-cancel)
                                    (i18n/label :t/you-will-start-from-scratch)
                                    #(re-frame/dispatch [:navigate-back])))}}]
     [ui/title-with-description :t/intro-wizard-title2 :t/intro-wizard-text2]
     [ui/learn-more :t/about-names-title :t/about-names-content]
     [react/view
      {:style {:flex            1
               :justify-content :center}}
      [react/scroll-view
       {:style                   {:max-height 410}
        :content-container-style {:justify-content :flex-start}}
       (for [[acc accessibility-n] (map vector multiaccounts (range (count multiaccounts)))]
         (let [selected?  (= (:id acc) selected-id)
               public-key (get-in acc [:derived constants/path-whisper-keyword :public-key])]
           ^{:key public-key}
           [quo/list-item
            {:accessibility-label (keyword (str "select-account-button-" accessibility-n))
             :active              selected?
             :title               [quo/text
                                   {:number-of-lines     2
                                    :weight              :medium
                                    :ellipsize-mode      :middle
                                    :accessibility-label :username}
                                   (gfy/generate-gfy public-key)]
             :subtitle            [quo/text
                                   {:weight :monospace
                                    :color  :secondary}
                                   (utils/get-shortened-address public-key)]
             :accessory           :radio
             :on-press            #(re-frame/dispatch [:intro-wizard/on-key-selected (:id acc)])
             :icon                [react/image
                                   {:source      {:uri (identicon/identicon public-key)}
                                    :resize-mode :cover
                                    :style       styles/multiaccount-image}]}]))]]
     [ui/next-button #(dispatch-and-chill [:navigate-to :select-key-storage] 300) false]]))

(defn get-your-keys-image
  []
  (let [dimensions (reagent/atom {})]
    (fn []
      ;;TODO this is not really the best way to do it, resize is visible, we need to find a better way
      [react/view
       {:on-layout (fn [^js e]
                     (reset! dimensions (js->clj (-> e .-nativeEvent .-layout) :keywordize-keys true)))
        :style     {:align-items     :center
                    :justify-content :center
                    :flex            1}}
       (let [image-size (- (min (:width @dimensions) (:height @dimensions)) 40)]
         [react/image
          {:source      (resources/get-theme-image :keys)
           :resize-mode :contain
           :style       {:width image-size :height image-size}}])])))

(defview get-your-keys
  []
  (letsubs [{:keys [processing?]} [:intro-wizard/choose-key]]
    [:<>
     [ui/title-with-description :t/intro-wizard-title1 :t/intro-wizard-text1]
     [get-your-keys-image]
     (if processing?
       [react/view {:style {:align-items :center}}
        [react/view {:min-height 46 :max-height 46 :align-self :stretch :margin-bottom 16}
         [react/activity-indicator
          {:animating true
           :size      :large}]]
        [react/text {:style (assoc (styles/wizard-text) :margin-top 20 :margin-bottom 16)}
         (i18n/label :t/generating-keys)]]
       [react/view {:style {:align-items :center}}
        [react/view {:style (assoc styles/bottom-button :margin-bottom 16)}
         [quo/button
          {:test-ID             :generate-keys
           ;:disabled            existing-account?
           :on-press            #(re-frame/dispatch [:generate-and-derive-addresses])
           :accessibility-label :onboarding-next-button}
          (i18n/label :t/generate-a-key)]]
        [react/view {:padding-vertical 8}
         [quo/button
          {:on-press #(re-frame/dispatch [:bottom-sheet/show-sheet :recover-sheet])
           :type     :secondary}
          (i18n/label :t/access-existing-keys)]]
        [react/text {:style (assoc (styles/wizard-text) :margin-top 20 :margin-bottom 16)}
         (i18n/label :t/this-will-take-few-seconds)]])]))
