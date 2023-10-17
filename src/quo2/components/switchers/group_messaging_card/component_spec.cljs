(ns quo2.components.switchers.group-messaging-card.component-spec
  (:require
    [quo2.components.switchers.group-messaging-card.view :as group-messaging-card]
    [quo2.components.switchers.utils :as utils]
    [test-helpers.component :as h]))

(def photos-list
  [{:source (js/require "../resources/images/mock2/photo1.png")}
   {:source (js/require "../resources/images/mock2/photo2.png")}
   {:source (js/require "../resources/images/mock2/photo3.png")}
   {:source (js/require "../resources/images/mock2/photo1.png")}
   {:source (js/require "../resources/images/mock2/photo2.png")}
   {:source (js/require "../resources/images/mock2/photo3.png")}])
(def sticker {:source (js/require "../resources/images/mock2/sticker.png")})
(def gif {:source (js/require "../resources/images/mock2/gif.png")})
(def coinbase-community (js/require "../resources/images/mock2/coinbase.png"))
(def link-icon (js/require "../resources/images/mock2/status-logo.png"))

(h/describe "Switcher: Group Messaging Card"
  (h/test "Default render"
    (h/render [group-messaging-card/view {}])
    (h/is-truthy (h/query-by-label-text :base-card)))

  (h/test "Avatar render"
    (h/render [group-messaging-card/view {:avatar true}])
    (h/is-truthy (h/query-by-label-text :group-avatar)))

  (h/test "Status: Read, Type: Message, Avatar: true"
    (h/render [group-messaging-card/view
               {:avatar  true
                :status  :read
                :type    :message
                :title   "Title"
                :content {:text "Last message"}}])
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-text (utils/subtitle :message nil)))
    (h/is-truthy (h/get-by-text "Last message")))

  (h/test "Status: Unread, Type: Message, Avatar: true"
    (h/render [group-messaging-card/view
               {:avatar true
                :status :unread
                :type   :message
                :title  "Title"}])
    (h/is-truthy (h/query-by-label-text :notification-dot)))

  (h/test "Status: Mention, Type: Message, Avatar: true"
    (h/render [group-messaging-card/view
               {:avatar  true
                :status  :mention
                :type    :message
                :title   "Title"
                :content {:mention-count 5}}])
    (h/is-truthy (h/get-by-test-id :counter-component)))

  (h/test "Status: Read, Type: Photo, Avatar: true"
    (h/render [group-messaging-card/view
               {:avatar  true
                :status  :read
                :type    :photo
                :title   "Title"
                :content {:photos photos-list}}])
    (h/is-truthy (h/get-by-text (utils/subtitle :photo {:photos photos-list}))))

  (h/test "Status: Read, Type: Stciker, Avatar: true"
    (h/render [group-messaging-card/view
               {:avatar  true
                :status  :read
                :type    :sticker
                :title   "Title"
                :content sticker}])
    (h/is-truthy (h/get-by-text (utils/subtitle :sticker nil)))
    (h/is-truthy (h/get-by-label-text :sticker)))

  (h/test "Status: Read, Type: Gif, Avatar: true"
    (h/render [group-messaging-card/view
               {:avatar  true
                :status  :read
                :type    :gif
                :title   "Title"
                :content gif}])
    (h/is-truthy (h/get-by-text (utils/subtitle :gif nil)))
    (h/is-truthy (h/get-by-label-text :gif)))

  (h/test "Status: Read, Type: Audio, Avatar: true"
    (h/render [group-messaging-card/view
               {:avatar  true
                :status  :read
                :type    :audio
                :title   "Title"
                :content {:duration "00:32"}}])
    (h/is-truthy (h/get-by-text (utils/subtitle :audio nil)))
    (h/is-truthy (h/get-by-text "00:32")))

  (h/test "Status: Read, Type: Community, Avatar: true"
    (h/render [group-messaging-card/view
               {:avatar  true
                :status  :read
                :type    :community
                :title   "Title"
                :content {:community-avatar coinbase-community
                          :community-name   "Coinbase"}}])
    (h/is-truthy (h/get-by-text (utils/subtitle :community nil)))
    (h/is-truthy (h/get-by-label-text :group-avatar))
    (h/is-truthy (h/get-by-text "Coinbase")))

  (h/test "Status: Read, Type: Link, Avatar: true"
    (h/render [group-messaging-card/view
               {:avatar  true
                :status  :read
                :type    :link
                :title   "Title"
                :content {:icon :placeholder
                          :text "Rolling St..."}}])
    (h/is-truthy (h/get-by-text (utils/subtitle :link nil)))
    (h/is-truthy (h/get-by-label-text :group-avatar))
    (h/is-truthy (h/get-by-text "Rolling St..."))))
