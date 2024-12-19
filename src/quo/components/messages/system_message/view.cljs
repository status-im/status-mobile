(ns quo.components.messages.system-message.view
  (:require
    [clojure.string :as string]
    [quo.components.avatars.icon-avatar :as icon-avatar]
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.markdown.text :as text]
    [quo.components.messages.system-message.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [utils.i18n :as i18n]))

(defn sm-icon
  [{:keys [icon color opacity]}]
  [rn/view
   {:style style/sm-icon-wrapper}
   [icon-avatar/icon-avatar
    {:size    :size-32
     :icon    icon
     :color   color
     :opacity opacity}]])

(defn sm-timestamp
  [timestamp theme]
  [rn/view style/sm-timestamp-wrapper
   [text/text
    {:size  :label
     :style (style/sm-timestamp-text theme)}
    timestamp]])

(defn sm-user-avatar
  [display-name photo-path]
  [rn/view style/sm-user-avatar-wrapper
   [user-avatar/user-avatar
    {:size              :xxxs
     :full-name         display-name
     :profile-picture   photo-path
     :ring?             false
     :status-indicator? false}]])

(defn split-text
  [label theme add-pred?]
  (let [label-vector (map-indexed vector (string/split label " "))]
    [rn/view {:style style/split-text-wrapper}
     (when add-pred?
       [text/text {} " "])
     (for [[indx item] label-vector]
       ^{:key indx}
       [text/text
        {:size  :paragraph-2
         :style (style/each-split-text theme indx label-vector)}
        item])]))

(defn system-message-base
  [{:keys [icon]} child]
  [rn/view {:style style/system-message-base-wrapper}
   [sm-icon icon]
   [rn/view {:style style/system-message-base-content-wrapper} child]])

(defn system-message-deleted
  [{:keys [label child timestamp]}]
  (let [theme (quo.theme/use-theme)]
    [system-message-base
     {:icon {:icon    :i/delete
             :color   :danger
             :opacity 5}}
     [rn/view {:style style/system-message-deleted-wrapper}
      (if child
        child
        [text/text
         {:size  :paragraph-2
          :style (style/system-message-deleted-text theme)}
         (or label (i18n/label :t/message-deleted))])
      [sm-timestamp timestamp theme]]]))

(defn system-message-contact
  [{:keys [display-name photo-path customization-color timestamp]} label icon]
  (let [theme (quo.theme/use-theme)]
    [system-message-base
     {:icon {:icon    icon
             :color   (or customization-color :primary)
             :opacity 5}}
     [rn/view
      {:style style/system-message-contact-wrapper}
      [rn/view {:style style/system-message-contact-account-wrapper}
       [sm-user-avatar display-name photo-path]
       [text/text
        {:weight          :semi-bold
         :number-of-lines 1
         :style           style/system-message-contact-account-name
         :size            :paragraph-2}
        display-name]]
      [split-text label theme true]
      [sm-timestamp timestamp theme]]]))

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

(defn system-message-contact-request
  [{:keys [display-name photo-path customization-color timestamp incoming?]}]
  (let [theme (quo.theme/use-theme)]
    [system-message-base
     {:icon {:icon    :i/add-user
             :color   (or customization-color :primary)
             :opacity 5}}
     [rn/view
      {:style style/system-message-contact-request-wrapper}
      (when-not incoming? [split-text "Contact request sent to" theme false])
      [rn/view {:style style/system-message-contact-request-account-wrapper}
       [sm-user-avatar display-name photo-path]
       [text/text
        {:weight          :semi-bold
         :number-of-lines 1
         :style           style/system-message-contact-request-account-name
         :size            :paragraph-2}
        display-name]]
      (when incoming? [split-text "sent you a contact request" theme true])
      [sm-timestamp timestamp theme]]]))

(defn system-message-group
  [{:keys [customization-color child]}]
  [system-message-base
   {:icon {:icon    :i/add-user
           :color   (or customization-color :primary)
           :opacity 5}}
   [rn/view child]])

(defn system-message-pinned
  [{:keys [pinned-by child customization-color timestamp]}]
  (let [theme (quo.theme/use-theme)]
    [system-message-base
     {:icon {:icon    :i/pin
             :color   (or customization-color :primary)
             :opacity 5}}
     [rn/view {:style style/system-message-pinned-wrapper}
      [rn/view
       {:style style/system-message-pinned-content-wrapper}
       [text/text
        {:weight          :semi-bold
         :number-of-lines 1
         :style           style/system-message-pinned-content-pinned-by
         :size            :paragraph-2}
        pinned-by]
       [split-text (i18n/label :t/pinned-a-message) theme true]
       [sm-timestamp timestamp theme]]
      (when child child)]]))

(defn f-system-message
  [{:keys [type animate-bg-color? bg-color-animation-duration on-long-press]
    :or   {bg-color-animation-duration 1000}
    :as   data}]
  (let [animated-bg-color (reanimated/use-shared-value
                           (if animate-bg-color?
                             style/system-message-deleted-animation-start-bg-color
                             style/system-message-deleted-animation-end-bg-color))
        wrapper           (if (or on-long-press animate-bg-color?)
                            reanimated/touchable-opacity
                            rn/view)
        animated-style    (reanimated/apply-animations-to-style
                           {:background-color animated-bg-color}
                           (assoc style/system-message-wrapper
                                  :background-color
                                  animated-bg-color))]

    (when animate-bg-color?
      (reanimated/animate-shared-value-with-delay
       animated-bg-color
       style/system-message-deleted-animation-end-bg-color
       0
       :linear
       bg-color-animation-duration))

    [wrapper
     {:style         (if (or on-long-press animate-bg-color?)
                       animated-style
                       style/system-message-wrapper)
      :on-long-press on-long-press}
     (case type
       :pinned          [system-message-pinned data]
       :deleted         [system-message-deleted data]
       :contact-request [system-message-contact-request data]
       :added           [system-message-added data]
       :removed         [system-message-removed data]
       :group           [system-message-group data]
       nil)]))

(defn system-message
  [message]
  [:f> f-system-message message])
