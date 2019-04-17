(ns status-im.extensions.capacities.components
  (:require [status-im.ui.components.react :as react]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.ui.components.button.view :as button]
            [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.extensions.capacities.map :as map-component]))

(defn button [{:keys [on-click enabled disabled] :as m} label]
  [button/secondary-button (merge {:disabled? (or (when (contains? m :enabled) (or (nil? enabled) (false? enabled))) disabled)}
                                  (when on-click {:on-press #(on-click {})})) label])

(defn on-input-change-text [on-change value]
  (on-change {:value value}))

(defn- on-input-change-text-delay [current on-change value delay]
  ;; If an input change handler has been already scheduled cancel it.
  (when-let [id @current]
    (js/clearTimeout id))
  (reset! current (js/setTimeout #(on-input-change-text on-change value) delay)))

(defn input [{:keys [keyboard-type style on-change change-delay placeholder placeholder-text-color selection-color
                     auto-focus on-submit default-value]}]
  [react/text-input (merge {:placeholder placeholder}
                           (when placeholder-text-color {:placeholder-text-color placeholder-text-color})
                           (when selection-color {:selection-color selection-color})
                           (when style {:style style})
                           (when keyboard-type {:keyboard-type keyboard-type})
                           (when auto-focus {:auto-focus auto-focus})
                           (when default-value {:default-value default-value})
                           (when on-submit
                             {:on-submit-editing #(on-submit {})})
                           (when on-change
                             {:on-change-text
                              (if change-delay
                                (let [current (atom nil)]
                                  #(on-input-change-text-delay current on-change % change-delay))
                                #(on-input-change-text on-change %))}))])

(defn touchable-opacity [{:keys [style on-press]} & children]
  (into [react/touchable-opacity (merge (when on-press {:on-press #(on-press {})})
                                        (when style {:style style}))] children))

(defn image [{:keys [source uri style]}]
  [react/image (merge {:style (merge {:width 100 :height 100} style)} {:source (if source source {:uri uri})})])

(defn link [{:keys [uri style open-in text]}]
  [react/text (merge {:style    {:color                colors/white
                                 :text-decoration-line :underline}
                      :on-press (case open-in
                                  :device #(.openURL react/linking uri)
                                  :status #(re-frame/dispatch [:browser.ui/open-in-status-option-selected uri])
                                  #(re-frame/dispatch [:browser.ui/message-link-pressed uri]))}
                     (when style {:style style}))
   (or text uri)])

(defn map-link
  "create a link-view which opens native map/navigation app with marker and label"
  [{:keys [text lat lng style]}]
  (let [uri (cond
              platform/ios? (str "http://maps.apple.com/?q=" (js/encodeURIComponent text) "&ll=" lat "," lng)
              platform/android? (str "geo:0,0?q=" lat "," lng "(" (js/encodeURIComponent text) ")")
              :else (str "http://www.openstreetmap.org/?mlat=" lat "&mlon=" lng))]
    (link {:uri     uri
           :text    text
           :style   style
           :open-in :device})))

(defn flat-list [{:keys [key data item-view]}]
  [list/flat-list {:data data :key-fn (or key (fn [_ i] (str i))) :render-fn item-view}])

(defn checkbox [{:keys [on-change checked]}]
  [react/view {:style {:background-color colors/white}}
   [checkbox/checkbox {:checked?        checked
                       :style           {:padding 0}
                       :on-value-change #(on-change {:value %})}]])

(defn activity-indicator-size [k]
  (condp = k
    :small "small"
    :large "large"
    nil))

(defn activity-indicator [{:keys [animating hides-when-stopped color size]}]
  [react/activity-indicator (merge (when animating {:animating animating})
                                   (when hides-when-stopped {:hidesWhenStopped hides-when-stopped})
                                   (when color {:color color})
                                   (when-let [size' (activity-indicator-size size)] {:size size'}))])

(defn picker [{:keys [style on-change selected enabled data]}]
  [react/picker {:style style :on-change #(on-change {:value %}) :selected selected :enabled enabled :data data}])

(defn- wrap-text-child [o]
  (if (ifn? o) o (str o)))

(defn text [o & children]
  (if (map? o)
    (into [react/text o] (map wrap-text-child children))
    (into [react/text {} o] (map wrap-text-child children))))

(defn- wrap-view-child [child]
  (if (vector? child) child [text {} child]))

(defn abstract-view [type o & children]
  (if (map? o)
    (into [type o] (map wrap-view-child children))
    (into [type {} (wrap-view-child o)] (map wrap-view-child children))))

(defn view [o & children]
  (apply abstract-view react/view o children))

(defn scroll-view [o & children]
  (apply abstract-view react/scroll-view o children))

(defn keyboard-avoiding-view [o & children]
  (apply abstract-view react/keyboard-avoiding-view o children))

(defn icon [{:keys [key] :as o}]
  [icons/icon key o])

;;CAPACITIES

(def all
  {'view                   {:data view}
   'scroll-view            {:data scroll-view :properties {:keyboard-should-persist-taps :keyword :content-container-style :map}}
   'keyboard-avoiding-view {:data react/keyboard-avoiding-view}
   'text                   {:data text}
   'touchable-opacity      {:data touchable-opacity :properties {:on-press :event}}
   'icon                   {:data icon :properties {:key :keyword :color :any}}
   'image                  {:data image :properties {:uri :string :source :string}}
   'input                  {:data input :properties {:on-change :event :placeholder :string :keyboard-type :keyword
                                                     :change-delay? :number :placeholder-text-color :any :selection-color :any
                                                     :auto-focus? :boolean :on-submit :event :default-value :any}}
   'button                 {:data button :properties {:enabled :boolean :disabled :boolean :on-click :event}}
   'link                   {:data link :properties {:uri :string :text? :string :open-in? {:one-of #{:device :status}}}}
   'list                   {:data flat-list :properties {:data :vector :item-view :view :key? :keyword}}
   'checkbox               {:data checkbox :properties {:on-change :event :checked :boolean}}
   'activity-indicator     {:data activity-indicator :properties {:animating :boolean :color :string :size :keyword :hides-when-stopped :boolean}}
   'picker                 {:data picker :properties {:on-change :event :selected :string :enabled :boolean :data :vector}}
   'nft-token-viewer       {:data transactions/nft-token :properties {:token :string}}
   'transaction-status     {:data transactions/transaction-status :properties {:outgoing :string :tx-hash :string}}
   'map                    {:data map-component/map-webview
                            :properties {:marker {:lng :number
                                                  :lat :number
                                                  :boundingbox {:lng1 :number
                                                                :lat1 :number
                                                                :lng2 :number
                                                                :lat2 :number}}
                                         :fly? :boolean
                                         :interactive? :boolean
                                         :on-change :event}}
   'map-link               {:data map-link :properties {:text :string :lng :any :lat :any}}})