(ns quo2.components.messages.system-message
  (:require [clojure.string :as string]
            [quo2.components.avatars.icon-avatar :as icon-avatar]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [utils.i18n :as i18n]))

(defn text-color
  [theme]
  (colors/theme-colors colors/neutral-100 colors/white theme))

(defn time-color
  [theme]
  (colors/theme-colors colors/neutral-40 colors/neutral-50 theme))

(defn sm-icon
  [{:keys [icon color opacity]}]
  [rn/view
   {:margin-right 8}
   [icon-avatar/icon-avatar
    {:size    :size-32
     :icon    icon
     :color   color
     :opacity opacity}]])

(defn sm-timestamp
  [timestamp theme]
  [rn/view {:margin-left 8}
   [text/text
    {:size  :label
     :style {:color          (time-color theme)
             :text-transform :none}}
    timestamp]])

(defn sm-user-avatar
  [display-name photo-path]
  [rn/view {:margin-right 4}
   [user-avatar/user-avatar
    {:size              :xxxs
     :full-name         display-name
     :profile-picture   photo-path
     :ring?             false
     :status-indicator? false}]])

(defn split-text
  [label theme add-pred?]
  (let [color        (text-color theme)
        label-vector (map-indexed vector (string/split label " "))]
    [rn/view {:style {:flex-direction :row :flex-shrink 0 :align-items :center}}
     (when add-pred?
       [text/text {} " "])
     (for [[indx item] label-vector]
       ^{:key indx}
       [text/text
        {:size  :paragraph-2
         :style {:color        color
                 :margin-right (if (= indx (dec (count label-vector)))
                                 0
                                 3)}}
        item])]))

(defn system-message-base
  [{:keys [icon]} child]
  [rn/view
   {:flex-direction :row
    :flex           1
    :align-items    :center}
   [sm-icon icon]
   [rn/view
    {:align-self     :center
     :flex-direction :row
     :flex           1}
    child]])

(defn system-message-deleted-internal
  [{:keys [label child theme timestamp]}]
  [system-message-base
   {:icon {:icon    :i/delete
           :color   :danger
           :opacity 5}}
   [rn/view {:style {:flex-direction :row :align-items :center}}
    (if child
      child
      [text/text
       {:size  :paragraph-2
        :style {:color (text-color theme)}}
       (or label (i18n/label :t/message-deleted))])
    [sm-timestamp timestamp theme]]])

(def system-message-deleted (quo.theme/with-theme system-message-deleted-internal))

(defn system-message-contact-internal
  [{:keys [display-name photo-path customization-color theme timestamp]} label icon]
  [system-message-base
   {:icon {:icon    icon
           :color   (or customization-color :primary)
           :opacity 5}}
   [rn/view
    {:flex-direction :row
     :align-items    :center
     :flex-shrink    1
     :flex-wrap      :nowrap}
    [rn/view {:flex-direction :row :align-items :center :flex-shrink 1}
     [sm-user-avatar display-name photo-path]
     [text/text
      {:weight          :semi-bold
       :number-of-lines 1
       :style           {:flex-shrink 1}
       :size            :paragraph-2}
      display-name]]
    [split-text label theme true]
    [sm-timestamp timestamp theme]]])

(def system-message-contact (quo.theme/with-theme system-message-contact-internal))

(defn system-message-added
  [data]
  [system-message-contact data (i18n/label :t/contact-request-is-now-a-contact) :i/add-user])

(defn system-message-removed
  [{:keys [incoming?] :as data}]
  [system-message-contact
   data
   (if incoming?
     (i18n/label :t/contact-request-removed-you-as-contact)
     (i18n/label :t/contact-request-removed-as-contact))
   :i/sad])

(defn system-message-contact-request-internal
  [{:keys [display-name photo-path customization-color theme timestamp incoming?]}]
  [system-message-base
   {:icon {:icon    :i/add-user
           :color   (or customization-color :primary)
           :opacity 5}}
   [rn/view
    {:flex-direction :row
     :align-items    :center
     :flex-shrink    1
     :flex-wrap      :nowrap}
    (when-not incoming? [split-text "Contact request sent to" theme false])
    [rn/view {:flex-direction :row :align-items :center :flex-shrink 1}
     [sm-user-avatar display-name photo-path]
     [text/text
      {:weight          :semi-bold
       :number-of-lines 1
       :style           {:flex-shrink 1}
       :size            :paragraph-2}
      display-name]]
    (when incoming? [split-text "sent you a contact request" theme true])
    [sm-timestamp timestamp theme]]])

(def system-message-contact-request (quo.theme/with-theme system-message-contact-request-internal))

(defn system-message-pinned-internal
  [{:keys [pinned-by child customization-color theme timestamp]}]
  [system-message-base
   {:icon {:icon    :i/pin
           :color   (or customization-color :primary)
           :opacity 5}}
   [rn/view {:style {:flex 1}}
    [rn/view
     {:flex-direction :row
      :align-items    :center
      :flex-wrap      :nowrap}
     [text/text
      {:weight          :semi-bold
       :number-of-lines 1
       :style           {:flex-shrink 1}
       :size            :paragraph-2}
      pinned-by]
     [split-text (i18n/label :pinned-a-message) theme true]
     [sm-timestamp timestamp theme]]
    (when child child)]])

(def system-message-pinned (quo.theme/with-theme system-message-pinned-internal))

(defn system-message
  [{:keys [type] :as data}]
  [rn/view {:padding-horizontal 12 :padding-vertical 8 :flex 1}
   (case type
     :pinned          [system-message-pinned data]
     :deleted         [system-message-deleted data]
     :contact-request [system-message-contact-request data]
     :added           [system-message-added data]
     :removed         [system-message-removed data]
     nil)])
