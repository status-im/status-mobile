(ns status-im.contexts.preview.quo.preview
  (:require
    [camel-snake-kebab.core :as camel-snake-kebab]
    cljs.pprint
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.blur :as blur]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.common :as common]
    [status-im.contexts.preview.quo.style :as style]
    utils.number
    [utils.re-frame :as rf])
  (:require-macros status-im.contexts.preview.quo.preview))

(defn- label-view
  [_ label theme]
  [rn/view {:style style/label-container}
   [rn/text {:style (style/label theme)}
    label]])

(defn- humanize
  [k]
  ;; We explicitly convert `k` to string because sometimes it's a number and
  ;; Clojure would throw an exception.
  (-> (if (keyword? k) k (str k))
      camel-snake-kebab/->kebab-case-keyword
      name
      (string/replace "-" " ")
      string/capitalize))

(defn- key->boolean-label
  [k]
  (let [label (humanize k)]
    (if (string/ends-with? label "?")
      label
      (str label "?"))))

(defn- key->text-label
  [k]
  (str (humanize k) ":"))

(defn- customizer-boolean
  [{:keys [label state] :as args}]
  (let [theme       (quo.theme/use-theme)
        label       (or label (key->boolean-label (:key args)))
        field-value (reagent/cursor state [(:key args)])
        active?     @field-value]
    [rn/view {:style style/field-row}
     [label-view state label theme]
     [rn/view {:style (style/boolean-container)}
      [rn/pressable
       {:style    (style/boolean-button {:active? active? :left? true} theme)
        :on-press #(reset! field-value true)}
       [rn/text {:style (style/field-text active? theme)}
        "True"]]
      [rn/pressable
       {:style    (style/boolean-button {:active? (not active?) :left? false} theme)
        :on-press #(reset! field-value false)}
       [rn/text {:style (style/field-text (not active?) theme)}
        "False"]]]]))

(defn- customizer-text
  [{:keys [label state limit suffix] :as args} theme]
  (let [label       (or label (key->text-label (:key args)))
        field-value (reagent/cursor state [(:key args)])]
    [rn/view {:style style/field-row}
     [label-view state label theme]
     [rn/view {:style style/field-column}
      [rn/text-input
       (merge
        {:value               @field-value
         :show-cancel         false
         :style               (style/field-container false theme)
         :keyboard-appearance theme
         :on-change-text      (fn [text]
                                (reset! field-value (if (and suffix
                                                             (> (count text) (count @field-value)))
                                                      (str (string/replace text suffix "") suffix)
                                                      text))
                                (reagent/flush))}
        (when limit
          {:max-length limit}))]]]))

(defn- customizer-number
  [{:keys [label state default] :as args} theme]
  (let [label       (or label (key->text-label (:key args)))
        field-value (reagent/cursor state [(:key args)])]
    [rn/view {:style style/field-row}
     [label-view state label theme]
     [rn/view {:style style/field-column}
      [rn/text-input
       (merge
        {:value               (str @field-value)
         :show-cancel         false
         :style               (style/field-container false theme)
         :keyboard-appearance theme
         :on-change-text      (fn [text]
                                (reset! field-value (utils.number/parse-int text default))
                                (reagent/flush))})]]]))

(defn- find-selected-option
  [id v]
  (first (filter #(= (:key %) id) v)))

(defn- customizer-select-modal
  [{:keys [open options field-value]}]
  (let [theme (quo.theme/use-theme)]
    [rn/modal
     {:visible                @open
      :on-request-close       #(reset! open false)
      :status-bar-translucent true
      :transparent            true
      :animation              :slide}
     [rn/view {:style (style/modal-overlay theme)}
      [rn/view {:style (style/modal-container theme)}
       [rn/scroll-view {:shows-vertical-scroll-indicator false}
        (doall
         (for [{k :key v :value} options
               :let              [v (or v (humanize k))]]
           ^{:key k}
           [rn/pressable
            {:style    (style/select-option (= @field-value k) theme)
             :on-press (fn []
                         (reset! open false)
                         (reset! field-value k))}
            [rn/text {:style (style/field-text (= @field-value k) theme)}
             v]]))]
       [rn/view {:style (style/footer theme)}
        [rn/pressable
         {:style    (style/select-button theme)
          :on-press (fn []
                      (reset! field-value nil)
                      (reset! open false))}
         [rn/text {:style (style/field-text false theme)}
          "Clear"]]
        [rn/view {:style {:width 16}}]
        [rn/touchable-opacity
         {:style    (style/select-button theme)
          :on-press #(reset! open false)}
         [rn/text {:style (style/field-text false theme)}
          "Close"]]]]]]))

(defn- customizer-select-button
  [{:keys [open selected-option]} theme]
  [rn/pressable
   {:style    (style/select-container theme)
    :on-press #(reset! open true)}
   [rn/text
    {:style           (style/field-select theme)
     :number-of-lines 1}
    (if selected-option
      (or (:value selected-option) (humanize (:key selected-option)))
      "Select option")]
   [rn/view
    [quo/icon :i/chevron-right]]])

(defn- customizer-select
  []
  (let [open (reagent/atom nil)]
    (fn [{:keys [label state options] :as args}]
      (let [theme           (quo.theme/use-theme)
            label           (or label (key->text-label (:key args)))
            field-value     (reagent/cursor state [(:key args)])
            selected-option (find-selected-option @field-value options)]
        [rn/view {:style style/field-row}
         [label-view state label theme]
         [rn/view {:style style/field-column}
          [customizer-select-modal
           {:open        open
            :options     options
            :field-value field-value}]
          [customizer-select-button {:open open :selected-option selected-option} theme]]]))))

(defn- customizer-multi-select-modal
  [{:keys [open-atom options selected-keys-atom]}]
  (let [theme (quo.theme/use-theme)]
    [rn/modal
     {:visible                @open-atom
      :on-request-close       #(reset! open-atom false)
      :status-bar-translucent true
      :transparent            true
      :animation              :slide}
     [rn/view {:style (style/modal-overlay theme)}
      [rn/view {:style (style/modal-container theme)}
       [rn/scroll-view {:shows-vertical-scroll-indicator false}
        (doall
         (for [{k :key v :value} options
               :let              [v (or v (humanize k))]]
           ^{:key k}

           (let [checked?   (boolean (some #(= k %) @selected-keys-atom))
                 remove-key (fn [v] (filterv #(not= % k) v))
                 on-press   (fn []
                              (swap! selected-keys-atom
                                (if checked? remove-key conj)
                                k))]
             [rn/pressable
              {:style    (style/multi-select-option theme)
               :on-press on-press}
              [rn/text {:style (style/field-text false theme)} v]
              [quo/selectors
               {:type      :checkbox
                :checked?  checked?
                :on-change on-press}]])))]
       [rn/view {:style (style/footer theme)}
        [rn/pressable
         {:style    (style/select-button theme)
          :on-press (fn []
                      (reset! selected-keys-atom nil)
                      (reset! open-atom false))}
         [rn/text {:style (style/field-text false theme)}
          "Clear"]]
        [rn/view {:style {:width 16}}]
        [rn/touchable-opacity
         {:style    (style/select-button theme)
          :on-press #(reset! open-atom false)}
         [rn/text {:style (style/field-text false theme)}
          "Close"]]]]]]))

(defn filter-by-keys
  [items ks]
  (filter (fn [item]
            (some #(= (:key item) %) ks))
          items))

(defn- customizer-multi-select-button
  [{:keys [open selected-options]} theme]
  [rn/pressable
   {:style    (style/select-container theme)
    :on-press #(reset! open true)}
   [rn/text
    {:style           (style/field-select theme)
     :number-of-lines 1}
    (if (seq selected-options)
      (string/join ", " (map :value selected-options))
      "Select options")]
   [rn/view
    [quo/icon :i/chevron-right]]])

(defn- customizer-multi-select
  []
  (let [open (reagent/atom nil)]
    (fn [{:keys [label state options] :as args}]
      (let [theme            (quo.theme/use-theme)
            label            (or label (key->text-label (:key args)))
            selected-keys    (reagent/cursor state [(:key args)])
            selected-options (filter-by-keys options @selected-keys)]
        [rn/view {:style style/field-row}
         [label-view state label theme]
         [rn/view {:style style/field-column}
          [customizer-multi-select-modal
           {:open-atom          open
            :selected-keys-atom selected-keys
            :options            options}]
          [customizer-multi-select-button {:open open :selected-options selected-options} theme]]]))))

(defn customizer
  [state descriptors theme]
  [rn/view
   {:style {:flex-shrink        1
            :padding-horizontal 20}}
   (doall
    (for [desc descriptors
          :let [desc-path  (:path desc)
                new-state  (if desc-path
                             (reagent/cursor state desc-path)
                             state)
                descriptor (assoc desc :state new-state)]]
      ^{:key (:key desc)}
      [:<>
       (case (:type desc)
         :boolean      [customizer-boolean descriptor]
         :text         [customizer-text descriptor theme]
         :number       [customizer-number descriptor theme]
         :select       [customizer-select descriptor]
         :multi-select [customizer-multi-select descriptor]
         nil)]))])

(defn customization-color-option
  ([]
   (customization-color-option {}))
  ([{:keys [feng-shui?] :as opts}]
   (merge {:key     :customization-color
           :type    :select
           :options (->> (merge colors/customization (when feng-shui? {:feng-shui nil}))
                         keys
                         sort
                         (map (fn [k]
                                {:key k :value (string/capitalize (name k))})))}
          opts)))

(defn blur-view
  [{:keys [show-blur-background? image height blur-view-props style theme]} & children]
  [rn/view
   {:style {:flex             1
            :padding-vertical 16}}
   (when show-blur-background?
     [rn/view
      {:style {:height        (or height 100)
               :border-radius 16
               :overflow      :hidden}}
      [rn/image
       {:source (or image (resources/get-mock-image :dark-blur-bg))
        :style  {:height "100%" :width "100%"}}]
      [blur/view
       (merge {:style         {:position :absolute
                               :top      0
                               :bottom   0
                               :left     0
                               :right    0}
               :blur-amount   10
               :overlay-color (colors/theme-colors colors/white-opa-70 colors/neutral-80-opa-80 theme)}
              blur-view-props)]])
   (into [rn/view
          {:style (merge {:position           :absolute
                          :top                32
                          :padding-horizontal 16}
                         style)}]
         children)])

(defn- f-preview-container
  [{:keys [title state descriptor blur? blur-dark-only?
           component-container-style
           blur-container-style blur-view-props blur-height show-blur-background? full-screen?]
    :or   {blur-height 200}}
   & children]
  (let [theme (quo.theme/use-theme)
        title (or title (rf/sub [:view-id]))]
    (rn/use-effect (fn []
                     (when blur-dark-only?
                       (if blur?
                         (rf/dispatch [:theme/switch :dark])
                         (rf/dispatch [:theme/switch :light]))))
                   [blur? blur-dark-only?])
    [rn/view
     {:style {:top  (safe-area/get-top)
              :flex 1}}
     [common/navigation-bar {:title title}]
     [rn/scroll-view
      {:style                           (style/panel-basic theme)
       :shows-vertical-scroll-indicator false
       :content-container-style         (when full-screen? {:flex 1})}
      [:<>
       [rn/pressable
        {:style    (when full-screen? {:flex 1})
         :on-press rn/dismiss-keyboard!}
        (when descriptor
          [rn/view {:style style/customizer-container}
           [customizer state descriptor theme]])
        (if blur?
          [rn/view {:style (merge style/component-container component-container-style)}
           (into [blur-view
                  {:theme                 theme
                   :show-blur-background? show-blur-background?
                   :height                blur-height
                   :style                 (merge {:width     "100%"
                                                  :flex-grow 1}
                                                 (when-not show-blur-background?
                                                   {:padding-horizontal 0
                                                    :top                0})
                                                 blur-container-style)
                   :blur-view-props       (merge {:blur-type theme}
                                                 blur-view-props)}]
                 children)]
          (into [rn/view {:style (merge style/component-container component-container-style)}]
                children))]
       (when state
         (let [decr-state (if descriptor (select-keys @state (mapv :key (flatten descriptor))) @state)
               state-str  (with-out-str (cljs.pprint/pprint decr-state))]
           [rn/view {:style {:margin 50}}
            [quo/text {:style {:margin-bottom 10}} "State map (click on map to copy)"]
            [rn/pressable
             {:on-press #(clipboard/set-string state-str)}
             [quo/text state-str]]]))]]]))

(defn preview-container
  [& args]
  (into [:f> f-preview-container] args))
