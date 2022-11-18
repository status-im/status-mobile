(ns status-im.ui2.screens.common.contact-list.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.ui2.screens.chat.components.contact-item.view :as contact-item]
            [quo2.core :as quo2]
            [utils.re-frame :as rf]))

(defn prepare-contacts [contacts]
  (let [data (atom {})]
    (doseq [i (range (count contacts))]
      (let [first-char (get (:alias (nth contacts i)) 0)]
        (if-not (contains? @data first-char)
          (swap! data #(assoc % first-char {:title first-char :data [(nth contacts i)]}))
          (swap! data #(assoc-in % [first-char :data] (conj (:data (get @data first-char)) (nth contacts i)))))))
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

(defn contact-list [{:keys [icon]}]
  (let [contacts (rf/sub [:contacts/active])
        contacts (prepare-contacts contacts)]
    [rn/section-list
     {:key-fn                         :title
      :sticky-section-headers-enabled false
      :sections                       contacts
      :render-section-header-fn       contacts-section-header
      :render-fn                      (fn [item]
                                        [contact-item/contact-item item {:icon icon}])}]))
