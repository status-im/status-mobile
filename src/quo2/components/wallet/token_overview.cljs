(ns quo2.components.wallet.token-overview
  (:require
   [quo2.foundations.colors :as colors]
   [react-native.core :as rn]
   [clojure.string :as string]
   [quo2.components.markdown.text :as text]
   [quo2.components.icon :as icons]))

(def container-style {:display :flex :width "100%" :padding-left 20 :padding-right 20 :padding-top 12 :padding-bottom 12})

(defn price-direction [price-change increase decrease neutral]
  (cond
    (pos? price-change) increase
    (neg? price-change) decrease
    :else neutral))

(defn price-color [direction]
  (price-direction direction colors/success-50 colors/danger-50 colors/neutral-50))

(defn divider [direction]
  [rn/view {:style  {:height 10
                     :margin-left 4
                     :margin-right 4
                     :width 1
                     :background-color (price-direction direction colors/success-50-opa-40 colors/danger-50-opa-40 colors/divider-light)}}])

(defn get-direction [percentage-change]
  (if (zero? (js/parseInt percentage-change)) 0
      (/ (js/parseInt percentage-change) (js/Math.abs  (js/parseInt percentage-change)))))

(defn trim-minus [percentage-change]  (if (= (first percentage-change) "-") (string/join (rest percentage-change)) percentage-change))

(defn token-price
  "[token-price opts \"label\"]
   opts
   {
    :currency :currency-key
    :price :string
    :percentage-change :string
    }"
  []
  (fn
    [{:keys [currency price percentage-change label] :or {price "0.00" percentage-change "0.0"}}]
    (let [direction (get-direction percentage-change)]
      [rn/view {:style container-style}
       [text/text  {:number-of-lines 1
                    :size  :paragraph-2} label]
       [text/text  {:style {:margin-top 4}
                    :weight :semi-bold
                    :number-of-lines 1
                    :size  :heading-2} (str currency price)]

       [rn/view {:style {:display :flex :flex-direction :row :margin-top 6 :align-items :center}}
        (when (not (zero? direction)) [icons/icon (if (>= direction 0) :i/price-increase12 :i/price-decrease12)
                                       {:no-color true
                                        :width 14
                                        :height 14
                                        :container-style {:margin-right 4}}])
        [text/text  {:number-of-lines 1
                     :weight :medium
                     :size  :paragraph-2
                     :style {:color (price-color direction)}}
         (str (trim-minus percentage-change) "%")]]])))

(defn token-balance
  "[token-balance opts \"label\"]
   opts
   {
    :token string
    :token-img-src :token-img-src
    :currency :currency-key
    :account-balance :string
    :price :string
    :percentage-change :string
    }"
  []
  (fn [{:keys [token token-img-src currency account-balance price percentage-change] :or {account-balance "0.00" price "0.00" percentage-change "0.0"}}]
    (let [direction (get-direction percentage-change)]
      [rn/view {:style container-style}
       [text/text  {:weight :regular
                    :number-of-lines 1
                    :size  :paragraph-1} (str "Account " token " Balance")]
       [rn/view {:style {:display :flex :flex-direction :row :flex 1 :justify-content :space-between}}
        [text/text  {:number-of-lines 1
                     :weight :semi-bold
                     :size  :heading-1} (str currency account-balance)]
        [rn/image {:source token-img-src
                   :style {:height 32
                           :width 32}}]]
       [rn/view {:style {:display :flex :flex-direction :row :margin-top 6 :align-items :center}}
        (when (not (zero? direction)) [icons/icon (if (pos? direction) :i/price-increase :i/price-decrease)
                                       {:no-color true
                                        :size 12
                                        :container-style {:margin-right 4}}])
        [text/text  {:number-of-lines 1
                     :weight :medium
                     :size  :paragraph-2
                     :style {:color  (price-color direction)}} (str currency price)]
        [divider direction]
        [text/text {:number-of-lines 1
                    :weight :medium
                    :size  :paragraph-2
                    :style {:color (price-color direction)}} (str (trim-minus percentage-change) "%")]]])))

