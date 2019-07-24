(ns status-im.ui.screens.announcements.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.fx :as fx]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.colors :as colors]))

(fx/defn hide-public-launch-banner
  {:events [:announcements.ui/banner-close-button-pressed]}
  [{:keys [db] :as cofx}]
  (let [settings (get-in db [:account/account :settings])]
    (accounts.update/update-settings
     cofx (assoc settings :show-public-launch-banner? false) {})))

(views/defview public-launch-banner [style]
  (views/letsubs [{:keys [settings]} [:account/account]]
    (let [show-banner? (or (nil? (:show-public-launch-banner? settings))
                           (:show-public-launch-banner? settings))]
      (when show-banner?
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch [:navigate-to :public-launch-announcement])}
         [react/view {:background-color colors/white}
          [react/view (merge style
                             {:background-color    colors/green-transparent-10
                              :justify-content     :space-between
                              :flex-direction      :row
                              :border-top-width    1
                              :border-top-color    colors/gray-light
                              :border-bottom-width 1
                              :border-bottom-color colors/gray-light})
           [react/image {:source (:small-badge resources/ui)
                         :style  {:height 64}}]
           [react/view {:flex            1
                        :margin-vertical 10
                        :margin-left     -4}
            [react/text {:style {:typography  :main-medium
                                 :line-height 22}}
             (i18n/label :t/status-launch-banner-title)]
            [react/text {:style {:line-height 22}}
             (i18n/label :t/stauts-launch-banner-text)]]
           [react/touchable-highlight
            {:style    {:width           44
                        :height          44
                        :align-items     :center
                        :justify-content :center}
             :on-press #(re-frame/dispatch
                         [:announcements.ui/banner-close-button-pressed])}
            [react/view {:width            21
                         :height           21
                         :border-radius    12
                         :background-color colors/gray
                         :align-items      :center
                         :justify-content  :center}
             [icons/icon :main-icons/close {:color  colors/white
                                            :width  19
                                            :height 19}]]]]]]))))

(defn public-launch-announcement []
  [react/view {:flex 1}
   [status-bar/status-bar {:type :modal-white}]
   [react/scroll-view
    [react/view {:justify-content :space-between
                 :flex-direction  :row}
     [react/touchable-highlight {:on-press            #(re-frame/dispatch [:navigate-back])
                                 :style               {:width           56
                                                       :height          56
                                                       :justify-content :center
                                                       :align-items     :center}
                                 :accessibility-label :done-button}
      [icons/icon :main-icons/close {}]]
     [react/view {:flex 1
                  :margin-right    56
                  :justify-content :center
                  :align-items     :center}
      [react/image {:source (:large-badge resources/ui)
                    :style  {:margin-top 4}}]]]
    [react/text {:style {:margin-top    0
                         :margin-bottom 8
                         :typography    :header
                         :text-align    :center}}
     (i18n/label :t/status-launch-modal-title)]
    [react/text {:style {:margin-top        8
                         ;; :color colors/gray
                         :line-height       21
                         :font-size         15
                         :margin-horizontal 16
                         :text-align        :center}}
     (i18n/label :t/status-launch-modal-text)]
    [react/text {:style {:color             colors/gray
                         :margin-horizontal 16
                         :margin-top        22
                         :margin-bottom     6
                         :font-size         15
                         :text-align        :left}}
     (i18n/label :t/status-launch-modal-h2-what-new)]
    [react/view {:flex-direction    :row
                 :margin-horizontal 16
                 :margin-top   6
                 :align-items       :center}
     [react/text {:style {:font-size 20}}
      "🔑"]
     [react/text {:style {:flex         1
                          :margin-left  8}}
      (i18n/label :t/status-launch-modal-new-key)]]
    [react/view {:flex-direction    :row
                 :margin-horizontal 16
                 :margin-top 8
                 :align-items       :center}
     [react/text {:style {:font-size 20}}
      "🗂"]
     [react/text {:style {:flex         1
                          :margin-left  8}}
      (i18n/label :t/status-launch-modal-multi-account)]]
    [react/view {:flex-direction    :row
                 :margin-horizontal 16
                 :margin-top 8
                 :align-items       :center}
     [react/text {:style {:font-size 20}}
      "💳"]
     [react/text {:style {:flex         1
                          :margin-left  8}}
      (i18n/label :t/status-launch-modal-keycard)]]
    [react/view {:flex-direction    :row
                 :margin-horizontal 16
                 :margin-top 8
                 :align-items       :center}
     [react/text {:style {:font-size 20}}
      "💬"]
     [react/text {:style {:flex         1
                          :margin-left  8}}
      (i18n/label :t/status-launch-modal-ens-in-chat)]]
    [react/view {:flex-direction    :row
                 :margin-horizontal 16
                 :margin-top 8
                 :align-items       :center}
     [react/text {:style {:font-size 20}}
      "🏇"]
     [react/text {:style {:flex         1
                          :margin-left  8}}
      (i18n/label :t/status-launch-modal-perf-improvements)]]
    [react/text {:style {:color             colors/gray
                         :margin-horizontal 16
                         :margin-top        6
                         :font-size         15
                         :text-align        :left}}
     (i18n/label :t/status-launch-modal-much-more)]
    [react/view {:background-color    colors/red-transparent-10
                 :border-top-width    1
                 :padding-horizontal  16
                 :padding-top         10
                 :margin-top 12
                 :padding-bottom      12
                 :margin-bottom       24
                 :border-top-color    colors/gray-light
                 :border-bottom-width 1
                 :border-bottom-color colors/gray-light}
     [react/text {:style {:typography  :main-medium
                          :line-height 22
                          :color      colors/red}}
      (i18n/label :t/status-launch-modal-before-you-update-title)]
     [react/text {:style {:line-height 22}}
      (i18n/label :t/status-launch-modal-before-you-update-text1)]
     [react/text {:style {:typography :main-semibold
                          :line-height 22}}
      (i18n/label :t/status-launch-modal-before-you-update-text2)]
     [react/text {:style {:line-height 22}}
      (i18n/label :t/status-launch-modal-before-you-update-text3)]
     [react/text {:style {:padding-top 10
                          :color colors/blue}
                  :on-press #(do
                               (re-frame/dispatch [:navigate-back])
                               (re-frame/dispatch [:browser.ui/open-url "https://v1-changes-explained.status.im"]))}
      (i18n/label :t/status-launch-modal-before-you-update-learn-more)]]
    [react/text {:style {:color             colors/gray
                         :margin-horizontal 16
                         :margin-bottom     6
                         :font-size         15
                         :text-align        :left}}
     (i18n/label :t/status-launch-modal-when-title)]
    [react/nested-text {:style {:line-height 22
                                :margin-horizontal 16}}
     (i18n/label :t/status-launch-modal-when-text1)
     [{:style {:color colors/blue} :on-press #(do
                                                (re-frame/dispatch [:navigate-back])
                                                (re-frame/dispatch [:chat.ui/start-public-chat "status"]))}
      (i18n/label :t/status-launch-modal-when-text2)]
     (i18n/label :t/status-launch-modal-when-text3)
     [{:style {:color colors/blue} :on-press #(do
                                                (re-frame/dispatch [:navigate-back])
                                                (re-frame/dispatch [:browser.ui/open-url "https://twitter.com/ethstatus"]))}
      (i18n/label :t/status-launch-modal-when-text4)]]
    [react/view {:align-items :center}
     [components.common/button {:button-style {:margin-vertical    30
                                               :padding-horizontal 32
                                               :justify-content    :center
                                               :align-items        :center}
                                :on-press     #(re-frame/dispatch [:navigate-back])
                                :label        (i18n/label :t/got-it)}]]]])