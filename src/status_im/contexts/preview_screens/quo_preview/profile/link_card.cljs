(ns status-im.contexts.preview-screens.quo-preview.profile.link-card
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(defn- initial-state
  [social]
  (case social
    :link      {:title               "Website"
                :icon                :social/link
                :customization-color :social/link
                :address             "bento.me/fracesca"}
    :facebook  {:title               "Facebook"
                :icon                :social/facebook
                :customization-color :social/facebook
                :address             "@francescab"}
    :github    {:title               "GitHub"
                :icon                :social/github
                :customization-color :social/github
                :address             "@francescab"}
    :instagram {:title               "Instagram"
                :icon                :social/instagram
                :customization-color :social/instagram
                :address             "@francescab"}
    :lens      {:title               "Lens"
                :icon                :social/lens
                :customization-color :social/lens
                :address             "@francescab"}
    :linkedin  {:title               "LinkedIn"
                :icon                :social/linkedin
                :customization-color :social/linkedin
                :address             "@francescab"}
    :mirror    {:title               "Mirror"
                :icon                :social/mirror
                :customization-color :social/mirror
                :address             "@francescab"}
    :opensea   {:title               "Opensea"
                :icon                :social/opensea
                :customization-color :social/opensea
                :address             "@francescab"}
    :pinterest {:title               "Pinterest"
                :icon                :social/pinterest
                :customization-color :social/pinterest
                :address             "@francescab"}
    :rarible   {:title               "Rarible"
                :icon                :social/rarible
                :customization-color :social/rarible
                :address             "@francescab"}
    :snapchat  {:title               "Snapchat"
                :icon                :social/snapchat
                :customization-color :social/snapchat
                :address             "@francescab"}
    :spotify   {:title               "Spotify"
                :icon                :social/spotify
                :customization-color :social/spotify
                :address             "@francescab"}
    :superrare {:title               "SuperRare"
                :icon                :social/superrare
                :customization-color :social/superrare
                :address             "@francescab"}
    :tumblr    {:title               "Tumblr"
                :icon                :social/tumblr
                :customization-color :social/tumblr
                :address             "@francescab"}
    :twitch    {:title               "Twitch"
                :icon                :social/twitch
                :customization-color :social/twitch
                :address             "@francescab"}
    :twitter   {:title               "Twitter"
                :icon                :social/twitter
                :customization-color :social/twitter
                :address             "@francescab"}
    :youtube   {:title               "YouTube"
                :icon                :social/youtube
                :customization-color :social/youtube
                :address             "@francescab"}
    nil))

(def descriptor
  [{:key     :link
    :type    :select
    :options [{:key :link}
              {:key :facebook}
              {:key :github}
              {:key :instagram}
              {:key :lens}
              {:key :linkedin}
              {:key :mirror}
              {:key :opensea}
              {:key :pinterest}
              {:key :rarible}
              {:key :snapchat}
              {:key :spotify}
              {:key :superrare}
              {:key :tumblr}
              {:key :twitch}
              {:key :twitter}
              {:key :youtube}]}])

(defn view
  []
  (let [state (reagent/atom {:link :link})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-bottom 50
                                    :width          200}}
       [quo/link-card
        (assoc (initial-state (:link @state))
               :on-press
               #(js/alert "pressed"))]])))
