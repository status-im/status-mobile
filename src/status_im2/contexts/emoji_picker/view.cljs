(ns status-im2.contexts.emoji-picker.view
  (:require [clojure.string :as string]
            [oops.core :as oops]
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
  [{:keys [id index active-category scroll-ref]}]
  (reset! active-category id)
  (some-> ^js @scroll-ref
          (.scrollToIndex #js
                           {:index    (emoji-picker.data/get-section-header-index-in-data index)
                            :animated false})))

(defn- handle-on-viewable-items-changed
  [{:keys [event active-category should-update-active-category?]}]
  (when should-update-active-category?
    (let [viewable-item (-> (oops/oget event "viewableItems")
                            transforms/js->clj
                            first
                            :item)
          header?       (and (map? viewable-item) (:header? viewable-item))
          section-key   (if header?
                          (:id viewable-item)
                          (:id (emoji-picker.data/emoji-group->category (-> viewable-item
                                                                            first
                                                                            :group))))]
      (when (and (some? section-key) (not= @active-category section-key))
        (reset! active-category section-key)))))

(defn- get-item-layout
  [_ index]
  #js
   {:length constants/item-height
    :offset (* constants/item-height index)
    :index  index})

(defn- section-header
  [{:keys [title]} {:keys [theme]}]
  [quo/divider-label
   {:tight?          false
    :container-style (style/section-header theme)}
   (i18n/label title)])

(defn- emoji-item
  [{:keys [unicode] :as emoji} col-index on-select close]
  (let [on-press          (fn []
                            (when on-select
                              (on-select unicode emoji))
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
  [row-data {:keys [on-select close]}]
  (into [rn/view {:style style/emoji-row-container}]
        (map-indexed
         (fn [col-index {:keys [hexcode] :as emoji}]
           ^{:key hexcode}
           [emoji-item emoji col-index on-select close])
         row-data)))

(defn- render-item
  [item _ _ render-data]
  (if (:header? item)
    [section-header item render-data]
    [emoji-row item render-data]))

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
      :initial-num-to-render           20
      :max-to-render-per-batch         20
      :render-fn                       render-item
      :get-item-layout                 get-item-layout
      :keyboard-dismiss-mode           :on-drag
      :keyboard-should-persist-taps    :handled
      :shows-vertical-scroll-indicator false
      :on-scroll-to-index-failed       identity
      :empty-component                 [empty-result]
      :on-scroll                       on-scroll
      :render-data                     {:close     close
                                        :theme     theme
                                        :on-select on-select}
      :content-container-style         style/list-container
      :viewability-config              {:item-visible-percent-threshold 100
                                        :minimum-view-time              200}
      :on-viewable-items-changed       on-viewable-items-changed}]))

(defn- footer
  [{:keys [theme active-category scroll-ref]}]
  (let [on-press (fn [id index]
                   (on-press-category
                    {:id              id
                     :index           index
                     :active-category active-category
                     :scroll-ref      scroll-ref}))]
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
          :data      emoji-picker.data/categories
          :on-press  on-press}]]])))

(defn- clear
  [{:keys [active-category filtered-data search-text]}]
  (reset! active-category constants/default-category)
  (reset! filtered-data nil)
  (reset! search-text ""))

(defn- view-internal
  [_]
  (let [{:keys [on-select]}       (rf/sub [:get-screen-params])
        scroll-ref                (atom nil)
        set-scroll-ref            #(reset! scroll-ref %)
        search-text               (reagent/atom "")
        filtered-data             (reagent/atom nil)
        active-category           (reagent/atom constants/default-category)
        clear-states              #(clear {:active-category active-category
                                           :filtered-data   filtered-data
                                           :search-text     search-text})
        search-emojis             (debounce/debounce
                                   (fn []
                                     (when (pos? (count @search-text))
                                       (reset! filtered-data (emoji-picker.utils/search-emoji
                                                              @search-text))))
                                   constants/search-debounce-ms)
        on-change-text            (fn [text]
                                    (if (string/blank? text)
                                      (clear-states)
                                      (do
                                        (reset! search-text text)
                                        (search-emojis))))
        on-viewable-items-changed (fn [event]
                                    (handle-on-viewable-items-changed
                                     {:event                          event
                                      :active-category                active-category
                                      :should-update-active-category? (nil? @filtered-data)}))]
    (fn [{:keys [theme] :as sheet-opts}]
      (let [search-active? (pos? (count @search-text))]
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
             :clearable?     search-active?
             :on-clear       clear-states}]]
          [render-list
           (merge {:filtered-data             @filtered-data
                   :set-scroll-ref            set-scroll-ref
                   :on-select                 on-select
                   :on-viewable-items-changed on-viewable-items-changed}
                  sheet-opts)]
          (when-not search-active?
            [footer
             {:theme           theme
              :active-category active-category
              :scroll-ref      scroll-ref}])]]))))

(def view (quo.theme/with-theme view-internal))
