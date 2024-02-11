(ns status-im.contexts.shell.activity-center.view
  (:require
    [oops.core :as oops]
    [quo.theme :as quo.theme]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.navigation :as navigation]
    [status-im.contexts.shell.activity-center.header.view :as header]
    [status-im.contexts.shell.activity-center.notification-types :as types]
    [status-im.contexts.shell.activity-center.notification.admin.view :as admin]
    [status-im.contexts.shell.activity-center.notification.community-kicked.view :as
     community-kicked]
    [status-im.contexts.shell.activity-center.notification.community-request.view :as
     community-request]
    [status-im.contexts.shell.activity-center.notification.contact-requests.view :as
     contact-requests]
    [status-im.contexts.shell.activity-center.notification.contact-verification.view :as
     contact-verification]
    [status-im.contexts.shell.activity-center.notification.membership.view :as membership]
    [status-im.contexts.shell.activity-center.notification.mentions.view :as mentions]
    [status-im.contexts.shell.activity-center.notification.reply.view :as reply]
    [status-im.contexts.shell.activity-center.style :as style]
    [status-im.contexts.shell.activity-center.tabs.empty-tab.view :as empty-tab]
    [utils.re-frame :as rf]))

(defn notification-component
  []
  (let [height               (atom 0)
        set-swipeable-height #(reset! height (oops/oget % "nativeEvent.layout.height"))]
    (fn [{:keys [type] :as notification} index _ {:keys [active-swipeable customization-color]}]
      (let [props {:height               height
                   :customization-color  customization-color
                   :active-swipeable     active-swipeable
                   :set-swipeable-height set-swipeable-height
                   :notification         notification
                   :extra-fn             (fn [] {:height @height :notification notification})}]
        [rn/view {:style (style/notification-container index)}
         (cond
           (= type types/contact-verification)
           [contact-verification/view props]

           (= type types/contact-request)
           [contact-requests/view props]

           (= type types/mention)
           [mentions/view props]

           (= type types/reply)
           [reply/view props]

           (= type types/admin)
           [admin/view props]

           (some types/membership [type])
           (condp = type
             types/private-group-chat [membership/view props]
             types/community-request  [community-request/view props]
             types/community-kicked   [community-kicked/view props]
             nil)

           :else
           nil)]))))

(defn view-internal
  []
  (let [active-swipeable (atom nil)
        current-screen   (rf/sub [:navigation/current-screen-id])]
    (rf/dispatch [:activity-center.notifications/fetch-first-page])
    (fn []
      (rn/use-effect #(quo.theme/handle-status-bar-screens current-screen))
      (let [notifications       (rf/sub [:activity-center/notifications])
            customization-color (rf/sub [:profile/customization-color])]
        [rn/view {:flex 1 :padding-top (navigation/status-bar-height)}
         [blur/view style/blur]
         [header/header]
         [rn/flat-list
          {:data                      notifications
           :render-data               {:active-swipeable    active-swipeable
                                       :customization-color customization-color}
           :content-container-style   {:flex-grow 1}
           :empty-component           [empty-tab/empty-tab]
           :key-fn                    :id
           :on-scroll-to-index-failed identity
           :on-end-reached            #(rf/dispatch [:activity-center.notifications/fetch-next-page])
           :render-fn                 notification-component}]]))))

(defn view
  []
  [:f> view-internal])
