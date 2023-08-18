(ns quo2.components.messages.system-message
  (:require [quo2.components.avatars.icon-avatar :as icon-avatar]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.markdown.text :as text]
            [quo2.components.messages.author.view :as author]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [quo2.theme :as quo.theme]
            [clojure.string :as string]))

(defn text-color
  [theme]
  (colors/theme-colors colors/neutral-100 colors/white theme))

(defn time-color
  [theme]
  (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))

(defn sm-icon
  [{:keys [icon color opacity]}]
  [rn/view
   {:margin-right 8}
   [icon-avatar/icon-avatar
    {:size    :medium
     :icon    icon
     :color   color
     :opacity opacity}]])

(defn sm-timestamp
  [timestamp]
  [rn/view {:margin-left 8 :margin-top 2}
   [text/text
    {:size  :label
     :style {:color          (time-color :time)
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
  (let [color (text-color theme)]
    [:<>
     (when add-pred?
       [text/text {} " "])
     (for [[indx item] (map-indexed vector (string/split label " "))]
       ^{:key indx}
       [text/text
        {:size  :paragraph-2
         :style {:color        color
                 :margin-right 3}}
        item])]))

(defn system-message-base
  [{:keys [icon timestamp]} child]
  [rn/view
   {:flex-direction :row
    :flex           1}
   [sm-icon icon]
   [rn/view
    {:align-self     :center
     :flex-direction :row
     :margin-right   40 ;; dirty hack, flexbox won't work as expected
     :flex           1}
    child
    [sm-timestamp timestamp]]])

(defn system-message-deleted-internal
  [{:keys [label child theme timestamp]}]
  [system-message-base
   {:icon      {:icon    :i/delete
                :color   :danger
                :opacity 5}
    :timestamp timestamp}
   (if child
     child
     [text/text
      {:size  :paragraph-2
       :style {:color (text-color theme)}}
      (or label (i18n/label :t/message-deleted))])])

(def system-message-deleted (quo.theme/with-theme system-message-deleted-internal))

(defn system-message-contact-internal
  [{:keys [display-name photo-path customization-color theme timestamp]} label icon]
  [system-message-base
   {:icon      {:icon    icon
                :color   (or customization-color :primary)
                :opacity 5}
    :timestamp timestamp}
   [rn/view
    {:flex-direction :row
     :align-items    :center
     :flex-wrap      :wrap}
    [rn/view {:flex-direction :row :align-items :center}
     [sm-user-avatar display-name photo-path]
     [text/text
      {:weight :semi-bold
       :size   :paragraph-2}
      display-name]]
    [split-text label theme true]]])

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
   {:icon      {:icon    :i/add-user
                :color   (or customization-color :primary)
                :opacity 5}
    :timestamp timestamp}
   [rn/view
    {:flex-direction :row
     :align-items    :center
     :flex-wrap      :wrap}
    (when-not incoming? [split-text "Contact request sent to" theme false])
    [rn/view {:flex-direction :row :align-items :center}
     [sm-user-avatar display-name photo-path]
     [text/text
      {:weight :semi-bold
       :size   :paragraph-2}
      display-name]]
    (when incoming? [split-text "sent you a contact request" theme true])]])

(def system-message-contact-request (quo.theme/with-theme system-message-contact-request-internal))

(defn system-message-pinned-internal
  [{:keys [pinned-by child customization-color theme timestamp]}]
  [system-message-base
   {:icon      {:icon    :i/pin
                :color   (or customization-color :primary)
                :opacity 5}
    :timestamp timestamp}
   [rn/view
    [rn/view
     {:flex-direction :row
      :flex-wrap      :wrap}
     [author/author {:primary-name pinned-by}]
     [split-text (i18n/label :pinned-a-message) theme true]]
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
