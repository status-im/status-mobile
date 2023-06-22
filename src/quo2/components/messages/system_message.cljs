(ns quo2.components.messages.system-message
  (:require [quo2.components.avatars.icon-avatar :as icon-avatar]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.markdown.text :as text]
            [quo2.components.messages.author.view :as author]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]))

(def themes-landed
  {:pinned  colors/primary-50-opa-5
   :added   colors/primary-50-opa-5
   :deleted colors/danger-50-opa-5})

(def themes
  {:light {:text colors/neutral-100
           :time colors/neutral-50
           :bg   {:default colors/white
                  :pressed colors/neutral-5
                  :landed  themes-landed}}
   :dark  {:text colors/white
           :time colors/neutral-40
           :bg   {:default colors/neutral-90
                  :pressed colors/neutral-80
                  :landed  themes-landed}}})

(defn get-color
  [& keys]
  (reduce (fn [acc k] (get acc k (reduced acc)))
          ((theme/get-theme) themes)
          (vec keys)))

(defn sm-timestamp
  [timestamp-str]
  [rn/view {:margin-left 8}
   [text/text
    {:size  :label
     :style {:color          (get-color :time)
             :text-transform :none}}
    timestamp-str]])

(defn sm-icon
  [{:keys [icon color opacity]}]
  [rn/view
   {:align-items  :center
    :margin-right 8}
   [icon-avatar/icon-avatar
    {:size    :medium
     :icon    icon
     :color   color
     :opacity opacity}]])

(defn sm-user-avatar
  [image]
  [rn/view {:margin-right 4}
   [user-avatar/user-avatar
    {:status-indicator? false
     :online?           false
     :size              :xxxs
     :profile-picture   image
     :ring?             false}]])

(defmulti system-message-content :type)

(defmethod system-message-content :deleted
  [{:keys [label timestamp-str labels child]}]
  [rn/view
   {:flex-direction :row
    :flex           1
    :align-items    :center}
   [sm-icon
    {:icon    :main-icons/delete
     :color   :danger
     :opacity 5}]
   [rn/view
    {:align-items    :baseline
     :flex-direction :row
     :flex           1
     :flex-wrap      :wrap}
    (if child
      child
      [text/text
       {:size  :paragraph-2
        :style {:color (get-color :text)}}
       (or (get labels label)
           label
           (:message-deleted labels))])
    [sm-timestamp timestamp-str]]])

(defmethod system-message-content :added
  [{:keys [state mentions timestamp-str labels]}]
  [rn/view
   {:align-items    :center
    :flex-direction :row}
   [sm-icon
    {:icon    :main-icons/add-user
     :color   :primary
     :opacity (if (= state :landed) 0 5)}]
   [sm-user-avatar (:image (first mentions))]
   [text/text
    {:weight :semi-bold
     :size   :paragraph-2}
    (:name (first mentions))]
   [text/text
    {:size  :paragraph-2
     :style {:color        (get-color :text)
             :margin-left  3
             :margin-right 3}}
    (:added labels)]
   [sm-user-avatar (:image (second mentions))]
   [text/text
    {:weight :semi-bold
     :size   :paragraph-2}
    (:name (second mentions))]
   [sm-timestamp timestamp-str]])

(defmethod system-message-content :pinned
  [{:keys [state pinned-by child timestamp-str labels]}]
  [rn/view
   {:flex-direction :row
    :flex           1
    :align-items    :center}
   [sm-icon
    {:icon    :main-icons/pin
     :color   :primary
     :opacity (if (= state :landed) 0 5)}]
   [rn/view
    {:flex-direction :column
     :flex           1}
    [rn/view
     {:align-items    :baseline
      :flex-direction :row
      :flex           1
      :flex-wrap      :wrap}
     [author/author
      {:primary-name pinned-by
       :style        {:margin-right 4}}]
     [rn/view
      [text/text
       {:size  :paragraph-2
        :style {:color (get-color :text)}}
       (:pinned-a-message labels)]]
     [sm-timestamp timestamp-str]]
    (when child
      child)]])

(defn- f-system-message
  [{:keys [type style non-pressable? animate-landing? labels on-long-press] :as message}]
  (let [sv-color (reanimated/use-shared-value
                  (get-color :bg (if animate-landing? :landed :default) type))]
    (when animate-landing?
      (reanimated/animate-shared-value-with-delay
       sv-color
       (get-color :bg :default type)
       0
       :linear
       1000))
    [reanimated/touchable-opacity
     {:on-press      #(when-not non-pressable?
                        (reanimated/set-shared-value sv-color (get-color :bg :pressed type)))
      :on-long-press on-long-press
      :style         (reanimated/apply-animations-to-style
                      {:background-color sv-color}
                      (merge {:flex-direction     :row
                              :flex               1
                              :padding-vertical   8
                              :padding-horizontal 12
                              :background-color   sv-color}
                             style))}
     [system-message-content message labels]]))

(defn system-message
  [message]
  [:f> f-system-message message])
