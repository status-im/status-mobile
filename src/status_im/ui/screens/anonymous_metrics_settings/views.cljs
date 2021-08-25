(ns status-im.ui.screens.anonymous-metrics-settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.animation :as animation]
            [status-im.react-native.resources :as resources]
            [status-im.anon-metrics.core :as anon-metrics]
            [status-im.ui.components.accordion :as accordion]
            [status-im.ui.components.react :as react]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.i18n.i18n :as i18n]
            [status-im.constants :refer [metrics-github-link]]
            [quo.design-system.spacing :as spacing]
            [status-im.ui.components.topbar :as topbar]
            [quo.core :as quo]))

(defn graphic-and-desc [{:keys [show-title?]}]
  [:<>
   [react/view {:align-items     :center
                :margin-vertical 25}
    [react/image {:source (resources/get-image :graph)}]]
   (when show-title?
     [quo/text {:size   :x-large
                :weight :bold
                :align  :center
                :style  {:margin-bottom 16}}
      (i18n/label :t/help-improve-status)])
   [react/touchable-highlight
    {:on-press            #(re-frame/dispatch [:navigate-to :anon-metrics-learn-more])
     :accessibility-label :learn-more-anchor}
    [react/nested-text
     {:style (merge {:color      colors/gray
                     :text-align :center}
                    (:large spacing/padding-horizontal))}
     (i18n/label :t/anonymous-usage-data-subtitle)
     [{:style {:color      colors/blue
               ;; :font-weight key doesn't work
               :fontWeight :bold}}
      (str " " (i18n/label :t/learn-more))]]]])

(defn setting-switch [enabled? on-press]
  [quo/list-item {:title               (i18n/label :t/share-anonymous-usage-data)
                  :active              enabled?
                  :accessibility-label :opt-in-switch
                  :accessory           :switch
                  :subtitle-max-lines  2
                  :on-press            on-press}])

(defn icon-list-item
  ([icon label]
   (icon-list-item icon {} label))
  ([icon icon-opts label]
   [react/view {:flex-direction    :row
                :margin-horizontal (:base spacing/spacing)
                :margin-vertical   (:tiny spacing/spacing)}
    [icons/icon icon (into icon-opts
                           {:container-style {:margin-top   1.2
                                              :margin-right (:tiny spacing/spacing)}})]
    [react/view {:style {:padding-right (:xx-large spacing/spacing)}}
     (if (string? label)
       [react/text label]
       label)]]))

(defn what-is-shared []
  [:<>
   [quo/list-header (i18n/label :t/what-is-shared)]
   (for [label [(i18n/label :t/anon-metrics-your-interactions)
                (i18n/label :t/anon-metrics-bg-activity)
                (i18n/label :t/anon-metrics-settings-and-prefs)]]
     ^{:key label}
     [icon-list-item :main-icons/info {:color colors/blue} label])])

(defn event-item [event]
  [accordion/section
   {:title [react/view {:flex           1
                        :flex-direction :row
                        :margin-bottom  (:base spacing/spacing)}
            [react/view {:style {:padding-right (:base spacing/spacing)}}
             (for [label ["event" "time" "os"]]
               ^{:key label}
               [react/text {:style {:color colors/gray}}
                label])]

            [react/view
             [react/text (:event event)]
             [react/text (:created_at event)]
             [react/text (:os event)]]]
    :content [react/view {:style {:background-color colors/gray-lighter
                                  :padding          (:small spacing/spacing)
                                  :border-radius    14}}
              [react/text (:value event)]]}])

(defn data-sheet-header-and-desc []
  [:<>
   [quo/text {:size   :x-large
              :weight :bold
              :align  :center
              :style  {:margin-vertical 16}}
    (i18n/label :t/data-collected)]
   [react/touchable-highlight
    {:on-press #(.openURL ^js react/linking metrics-github-link)}
    [react/nested-text
     {:style (:base spacing/padding-horizontal)}
     (i18n/label :t/data-collected-subtitle)
     [{:style {:color colors/blue}}
      (str " " (i18n/label :t/view-rules))]]]])

(defview view-data []
  (letsubs [events [:anon-metrics/events]
            total [:anon-metrics/total-count]
            fetching? [:anon-metrics/fetching?]]
    {:component-did-mount #(re-frame/dispatch [::anon-metrics/fetch-local-metrics
                                               {:clear-existing? true
                                                :limit           10
                                                :offset          0}])}
    [:<>
     [topbar/topbar {:modal?        true
                     :border-bottom false}]
     [react/scroll-view
      [data-sheet-header-and-desc]
      [quo/list-header (i18n/label :t/count-metrics-collected {:count total})]
      (when (pos? total)
        [react/view
         {:style (merge
                  {:border-width   1
                   :border-radius  8
                   :border-color   colors/gray-lighter
                   :padding-top    16
                   :padding-bottom 20
                   :margin         16}
                  (:base spacing/padding-horizontal))}
         (doall
          (map-indexed
           (fn [index event]
             ^{:key index}
             [event-item event])
           events))
         (if fetching?
           [react/activity-indicator {:size      :large
                                      :animating true}]
           [quo/button {:type                :primary
                        :disabled            (= (count events) total)
                        :accessibility-label :show-more-button
                        :on-press            #(re-frame/dispatch [::anon-metrics/fetch-local-metrics
                                                                  {:clear-existing? false
                                                                   :limit           10
                                                                   :offset          (count events)}])}
            (i18n/label :t/show-more)])])]]))

(defn view-data-button []
  [react/view {:flex        1
               :align-items :center
               :style       {:margin-top (:x-large spacing/spacing)}}
   [quo/button {:type                :primary
                :theme               :main
                :accessibility-label :view-data-button
                :on-press            #(re-frame/dispatch [:navigate-to :anon-metrics-view-data])}
    (i18n/label :t/view-data)]])

(defn desc-point-with-link [desc link-text link-href accessibility-label]
  [react/touchable-highlight {:on-press #(.openURL ^js react/linking link-href)
                              :accessibility-label accessibility-label}
   [react/view
    [react/text desc]
    [react/view {:flex-direction :row}
     [react/text {:style {:text-align :center
                          :color      colors/blue}}
      link-text]
     [icons/tiny-icon :tiny-icons/tiny-external
      {:color           colors/blue
       :container-style {:margin-left 4
                         :margin-top  4}}]]]])

(defn learn-more []
  [:<>
   [topbar/topbar {:modal? true
                   :border-bottom false}]
   [react/scroll-view
    [quo/text {:size   :x-large
               :weight :bold
               :align  :center
               :style  {:margin-top 16}}
     (i18n/label :t/about-sharing-data)]
    [react/text {:style (merge
                         (:base spacing/padding-horizontal)
                         (:small spacing/padding-vertical))}
     (i18n/label :t/about-sharing-data-subtitle)]
    [what-is-shared]
    [view-data-button]
    [quo/separator {:style {:margin-vertical (:base spacing/spacing)}}]
    [quo/list-header (i18n/label :t/how-it-works)]
    (for [label [[desc-point-with-link
                  (i18n/label :t/sharing-data-desc-1)
                  (i18n/label :t/view-rules)
                  metrics-github-link
                  :view-rules-anchor]
                 (i18n/label :t/sharing-data-desc-2)
                 (i18n/label :t/sharing-data-desc-3)
                 (i18n/label :t/sharing-data-desc-4)
                 ;; TODO (shivekkhurana): Enable this link when the public
                 ;; dashboard is live
                 ;; [desc-point-with-link
                 ;;  (i18n/label :t/sharing-data-desc-5)
                 ;;  (i18n/label :t/view-public-dashboard)
                 ;;  "https://github.com/"
                 ;; :view-dashboad-anchor]
                 (i18n/label :t/sharing-data-desc-6)]]
      ^{:key label}
      [icon-list-item :main-icons/arrow-right {:color colors/blue} label])
    [react/view {:style {:margin-bottom (:xx-large spacing/spacing)}}]]])

(defview settings []
  (letsubs [{:keys [:anon-metrics/should-send?]} [:multiaccount]]
    [:<>
     [topbar/topbar {:title (i18n/label :t/anonymous-usage-data)}]
     [react/scroll-view {:flex 1}
      [graphic-and-desc]
      [setting-switch
       should-send?
       #(re-frame/dispatch [:multiaccounts.ui/share-anonymous-usage-data-switched (not should-send?)])]
      [quo/separator]
      [what-is-shared]
      [react/view {:padding-bottom 80}
       [view-data-button]]]]))

(defn allow-or-not-actions []
  [react/view {:align-items :center
               :style       {:padding-bottom 80}}
   [quo/button {:type                :primary
                :theme               :main
                :accessibility-label :opt-in-button
                :on-press            #(re-frame/dispatch [::anon-metrics/opt-in true])}
    (i18n/label :t/allow-and-send)]
   [react/view {:style {:margin-top (:base spacing/spacing)}}]
   [quo/button {:type                :primary
                :theme               :main
                :accessibility-label :opt-out-button
                :on-press            #(re-frame/dispatch [::anon-metrics/opt-in false])}
    (i18n/label :t/no-thanks)]])

(defn thank-you-animation [overlay-opacity box-opacity]
  (fn []
    (animation/start
     (animation/parallel
      [(animation/spring
        overlay-opacity
        {:toValue         0.72
         :useNativeDriver true})
       (animation/spring
        box-opacity
        {:toValue         1
         :useNativeDriver true})]))))

(defview floating-thank-you []
  (letsubs [overlay-opacity (animation/create-value 1)
            box-opacity (animation/create-value 0.2)]
    {:component-did-mount (thank-you-animation overlay-opacity box-opacity)}
    [:<>
     [react/animated-view ; thank you container
      {:style {:position         :absolute
               :opacity          box-opacity
               :z-index          2
               :top              "40%"
               :padding          (:x-large spacing/spacing)
               :background-color colors/white
               :align-items      :center
               :align-self       :center}}
      [react/text {:style {:font-size 32}} "\uD83C\uDF89"]
      [quo/text {:size   :x-large
                 :weight :bold}
       (i18n/label :t/thank-you)]]
     [react/animated-view ; translucent overlay
      {:style {:position         :absolute
               :top              0
               :bottom           0
               :left             0
               :right            0
               :z-index          1
               :background-color colors/white
               :opacity          overlay-opacity}}]]))

(defview new-account-opt-in []
  (letsubs [show-thank-you? [:anon-metrics/show-thank-you?]]
    [react/scroll-view {:flex            1
                        :padding-vertical (:large spacing/spacing)
                        :flex-direction  :column}
     (when show-thank-you? [floating-thank-you])
     [graphic-and-desc {:show-title? true}]
     [react/view {:style {:margin-bottom (:small spacing/spacing)
                          :margin-top    (:large spacing/spacing)}}
      [quo/separator]]
     [what-is-shared]
     [react/view {:style (:large spacing/padding-vertical)}
      [quo/separator]]
     [allow-or-not-actions]]))

(comment
  (re-frame/dispatch [:navigate-back])
  (re-frame/dispatch [:navigate-to :metrics-learn-more])
  (re-frame/dispatch [:navigate-to :metrics-view-data])
  (re-frame/dispatch [:navigate-to :anon-metrics-opt-in {}]))
