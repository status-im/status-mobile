(ns status-im.ui2.screens.common.contact-list.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [status-im2.common.contact-list-item.view :as contact-list-item]
            [quo2.core :as quo2]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [oops.core :refer [oget]]
            [clojure.string :as str]))

(def query (reagent/atom ""))

(defn prepare-contacts [contacts]
  (let [data (atom {})]
    (doseq [i (range (count contacts))]
      (let [first-char (get (:alias (nth contacts i)) 0)]
        (when (or (empty? @query) (str/includes? (str/lower-case (:alias (nth contacts i))) (str/lower-case @query)))
          (if-not (contains? @data first-char)
            (swap! data #(assoc % first-char {:title first-char :data [(nth contacts i)]}))
            (swap! data #(assoc-in % [first-char :data] (conj (:data (get @data first-char)) (nth contacts i))))))))
    (swap! data #(sort @data))
    (vals @data)))

(defn contacts-section-header [{:keys [title]}]
  [rn/view {:style {:border-top-width   1
                    :border-top-color   colors/neutral-20
                    :padding-vertical   8
                    :padding-horizontal 20 :margin-top 8}}
   [quo2/text {:size   :paragraph-2
               :weight :medium
               :style  {:color colors/neutral-50}} title]])

(defn search-input []
  [rn/text-input {:placeholder "Search..."
                  :style       {:height             32
                                :padding-horizontal 20
                                :margin-vertical    12}
                  :on-change   (fn [e] (rf/dispatch [:contacts/search-query (oget e "nativeEvent.text")]))}])

(defn contact-list [{:keys [search?] :as data}]
  (let [
        ;contacts (rf/sub [:contacts/active])
        ;contacts (prepare-contacts contacts)
        contacts (rf/sub [:contacts/filtered-active-sections])
        ]
    ;(println "asdf" contactsx)
    [rn/section-list
     {:key-fn                         :title
      :sticky-section-headers-enabled false
      :sections                       contacts
      :render-section-header-fn       contacts-section-header
      :content-container-style        {:padding-bottom 120}
      :header                         (when search? (search-input))
      :sticky-header-indices [0]
      :render-fn                      (fn [item]
                                        [contact-list-item/contact-list-item item data])}]
    ))
