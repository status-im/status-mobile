(ns status-im2.contexts.emoji-picker.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [status-im2.contexts.emoji-picker.constants :as constants]
            [status-im2.contexts.emoji-picker.data :as emoji-picker.data]
            [status-im2.contexts.emoji-picker.style :as style]
            [status-im2.contexts.emoji-picker.utils :as emoji-picker.utils]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.transforms :as transforms]))

(defn- on-press-category
  [id index active-category scroll-ref]
  (reset! active-category id)
  (some-> ^js @scroll-ref
          (.scrollToIndex #js
                           {:index    (emoji-picker.data/get-section-header-index-in-data index)
                            :animated false})))

(defn- handle-on-viewable-items-changed
  [event active-category filtered-data]
  (when-not @filtered-data
    (let [viewable-item (-> (oops/oget event "viewableItems")
                            transforms/js->clj
                            first
                            :item)
          is-header?    (and (map? viewable-item) (:header? viewable-item))
          section-key   (if is-header?
                          (:id viewable-item)
                          (emoji-picker.data/section-group (get-in (vec viewable-item) [0 :group])))]
      (when (and (some? section-key) (not= @active-category section-key))
        (reset! active-category section-key)))))

(defn- get-item-layout
  [_ index]
  #js
   {:length constants/get-item-height
    :offset (* constants/get-item-height index)
    :index  index})

(defn- section-header
  [{:keys [title]} _ _ {:keys [theme]}]
  [quo/divider-label
   {:tight?          false
    :container-style (style/section-header theme)}
   (i18n/label title)])

(defn- emoji-item
  [{:keys [unicode] :as emoji} col-index callback close]
  (let [on-press          (fn []
                            (when callback
                              (callback unicode emoji))
                            (close))
        last-item-on-row? (= (inc col-index) constants/emojis-per-row)]
    (fn []
      [rn/pressable
       {:style    (style/emoji-container last-item-on-row?)
        :on-press on-press}
       [rn/text
        {:style                    {:font-size constants/emoji-size}
         :adjusts-font-size-to-fit true
         :allow-font-scaling       false}
        unicode]])))

(defn- emoji-row
  [row-data _ _ {:keys [callback close]}]
  (into [rn/view {:style style/emoji-row-container}]
        (map-indexed
         (fn [col-index {:keys [hexcode] :as emoji}]
           ^{:key (str hexcode col-index)}
           [emoji-item emoji col-index callback close])
         row-data)))

(defn- render-item
  [item index separator render-data]
  (if (:header? item)
    [section-header item index separator render-data]
    [emoji-row item index separator render-data]))

(defn- empty-result
  []
  [quo/empty-state
   {:title           (i18n/label :t/emoji-no-results-title)
    :description     (i18n/label :t/emoji-no-results-description)
    :placeholder?    true
    :container-style style/empty-results}])

(defn- render-list
  [{:keys [theme filtered-data on-viewable-items-changed scroll-enabled on-scroll on-select
           set-scroll-ref close]}]
  (let [data (if filtered-data filtered-data emoji-picker.data/flatten-data)]
    [gesture/flat-list
     {:ref                             set-scroll-ref
      :scroll-enabled                  @scroll-enabled
      :data                            data
      :initial-num-to-render           15
      :render-fn                       render-item
      :get-item-layout                 get-item-layout
      :keyboard-dismiss-mode           :on-drag
      :keyboard-should-persist-taps    :handled
      :shows-vertical-scroll-indicator false
      :on-scroll-to-index-failed       identity
      :empty-component                 [empty-result]
      :on-scroll                       on-scroll
      :render-data                     {:close    close
                                        :theme    theme
                                        :callback on-select}
      :content-container-style         style/list-container-style
      :viewability-config              {:wait-for-interaction           true
                                        :item-visible-percent-threshold 100
                                        :minimum-view-time              200}
      :on-viewable-items-changed       on-viewable-items-changed}]))

(defn- footer
  [theme active-category scroll-ref]
  (let [on-press #(on-press-category %1 %2 active-category scroll-ref)]
    (fn []
      [rn/view {:style style/category-container}
       [blur/view
        {:style         style/category-blur-container
         :blur-radius   (if platform/android? 20 10)
         :blur-amount   (if platform/ios? 20 10)
         :blur-type     (quo.theme/theme-value (if platform/ios? :light :xlight) :dark theme)
         :overlay-color (quo.theme/theme-value colors/white-70-blur colors/neutral-95-opa-70-blur theme)}
        [quo/showcase-nav
         {:state     :scroll
          :active-id @active-category
          :data      emoji-picker.data/list-data
          :on-press  on-press}]]])))

(defn- clear
  [active-category filtered-data search-text]
  (reset! active-category :people)
  (reset! filtered-data nil)
  (reset! search-text ""))

(defn- view-internal
  [_]
  (let [{:keys [on-select]}       (rf/sub [:get-screen-params])
        scroll-ref                (atom nil)
        set-scroll-ref            #(reset! scroll-ref %)
        search-text               (reagent/atom "")
        filtered-data             (reagent/atom nil)
        active-category           (reagent/atom :people)
        search-emojis             (debounce/debounce
                                   (fn []
                                     (when (pos? (count @search-text))
                                       (let [results         (emoji-picker.utils/search-emoji
                                                              @search-text)
                                             has-results?    (not-empty results)
                                             filtered-search (into []
                                                                   (when has-results?
                                                                     results))]
                                         (reset! filtered-data filtered-search))))
                                   constants/search-debounce-ms)
        on-change-text            (fn [text]
                                    (if (= text "")
                                      (clear active-category filtered-data search-text)
                                      (do
                                        (reset! search-text text)
                                        (search-emojis))))
        on-clear-text             (fn []
                                    (clear active-category filtered-data search-text))
        on-viewable-items-changed (fn [event]
                                    (handle-on-viewable-items-changed event
                                                                      active-category
                                                                      filtered-data))]
    (fn [{:keys [theme] :as sheet-opts}]
      (let [is-search-active? (pos? (count @search-text))]
        [rn/keyboard-avoiding-view
         {:style                    style/flex-spacer
          :keyboard-vertical-offset 8}
         [rn/view {:style style/flex-spacer}
          [rn/view {:style style/search-input-container}
           [quo/input
            {:small?         true
             :placeholder    (i18n/label :t/emoji-search-placeholder)
             :icon-name      :i/search
             :value          @search-text
             :on-change-text on-change-text
             :clearable?     is-search-active?
             :on-clear       on-clear-text}]]
          [render-list
           (merge {:filtered-data             @filtered-data
                   :set-scroll-ref            set-scroll-ref
                   :on-select                 on-select
                   :on-viewable-items-changed on-viewable-items-changed}
                  sheet-opts)]
          (when-not is-search-active?
            [footer theme active-category scroll-ref])]]))))

(def view (quo.theme/with-theme view-internal))
