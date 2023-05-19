(ns quo2.components.notifications.notification.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.notifications.notification.style :as style]
            [react-native.blur :as blur]
            [react-native.core :as rn]))

(defn header-container
  [& children]
  [into
   [rn/view {:accessibility-label :notification-header}] children])

(defn body-container
  [& children]
  [into [rn/view {:accessibility-label :notification-body}] children])

(defn avatar-container
  [& children]
  [into
   [rn/view
    {:style               style/avatar-container
     :accessibility-label :notification-avatar}]
   children])

(defn title
  ([text weight] (title text weight nil))
  ([text weight override-theme]
   [text/text
    {:size                :paragraph-1
     :weight              (or weight :semi-bold)
     :style               (style/title override-theme)
     :accessibility-label :notification-title}
    text]))

(defn message
  [text override-theme]
  [text/text
   {:size                :paragraph-2
    :style               (style/text override-theme)
    :accessibility-label :notification-content}
   text])

(defn- notification-container
  [{:keys [avatar header body container-style override-theme]}]
  [rn/view
   {:style (merge style/box-container container-style)}
   [blur/webview-blur
    {:style style/blur-container
     :blur-radius 10}]
   ;[blur/view
   ; {:style         style/blur-container
   ;  :blur-amount   13
   ;  :blur-radius   10
   ;  :blur-type     :transparent
   ;  :overlay-color :transparent}]
   [rn/view
    {:style (style/content-container override-theme)}
    avatar
    [rn/view
     {:style style/right-side-container}
     header
     body]]])

(defn notification
  [{title-text :title :keys [avatar header title-weight text body container-style override-theme]}]
  (let [header (or header
                   (when title-text
                     [title title-text title-weight override-theme]))
        header (when header [header-container header])
        body   (or body (when text [message text override-theme]))
        body   (when body [body-container body])
        avatar (when avatar [avatar-container avatar])]
    [notification-container
     {:avatar          avatar
      :header          header
      :body            body
      :container-style container-style
      :override-theme  override-theme}]))
