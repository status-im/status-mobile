(ns quo.components.notifications.notification.view
  (:require
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.markdown.text :as text]
    [quo.components.notifications.notification.style :as style]
    [quo.theme]
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
  [{:keys [multiline?]} & children]
  [into
   [rn/view
    {:style               (style/avatar-container {:multiline? multiline?})
     :accessibility-label :notification-avatar}]
   children])

(defn title
  ([text weight] (title text weight nil))
  ([text weight theme]
   [text/text
    {:size                :paragraph-1
     :weight              (or weight :semi-bold)
     :style               (style/title theme)
     :accessibility-label :notification-title}
    text]))

(defn message
  [text theme]
  [text/text
   {:size                :paragraph-2
    :weight              :medium
    :style               (style/text theme)
    :accessibility-label :notification-content}
   text])

(defn- notification-container
  [{:keys [avatar header body container-style theme]}]
  [rn/view
   {:style (merge style/box-container container-style)}
   [blur/view
    {:style         style/blur-container
     :blur-amount   13
     :blur-radius   10
     :blur-type     :transparent
     :overlay-color :transparent}]
   [rn/view
    {:style (style/content-container theme)}
    avatar
    [rn/view
     {:style style/right-side-container}
     header
     body]]])

(defn notification
  [{title-text :title :keys [user header title-weight text body container-style theme]}]
  (let [context-theme (or theme (quo.theme/get-theme))
        header        (or header
                          (when title-text
                            [title title-text title-weight theme]))
        header        (when header [header-container header])
        body          (or body (when text [message text theme]))
        body          (when body [body-container body])
        user-avatar   (when user (user-avatar/user-avatar user))
        avatar        (when user-avatar
                        [avatar-container
                         {:multiline? (and header body)}
                         user-avatar])]
    [quo.theme/provider {:theme context-theme}
     [notification-container
      {:avatar          avatar
       :header          header
       :body            body
       :container-style container-style
       :theme           theme}]]))
