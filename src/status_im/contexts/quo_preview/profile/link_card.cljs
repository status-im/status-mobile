(ns status-im.contexts.quo-preview.profile.link-card
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(defn- initial-state
  [social]
  (case social
    :link      {:title               "Website"
                :icon                :social/link
                :customization-color :link
                :on-press            #(js/alert "pressed")
                :address             "bento.me/fracesca"}
    :facebook  {:title               "Facebook"
                :icon                :social/facebook
                :customization-color :facebook
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :github    {:title               "GitHub"
                :icon                :social/github
                :customization-color :github
                :address             "@francescab"}
    :instagram {:title               "Instagram"
                :icon                :social/instagram
                :customization-color :instagram
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :lens      {:title               "Lens"
                :icon                :social/lens
                :customization-color :lens
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :linkedin  {:title               "LinkedIn"
                :icon                :social/linkedin
                :customization-color :linkedin
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :mirror    {:title               "Mirror"
                :icon                :social/mirror
                :customization-color :mirror
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :opensea   {:title               "Opensea"
                :icon                :social/opensea
                :customization-color :opensea
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :pinterest {:title               "Pinterest"
                :icon                :social/pinterest
                :customization-color :pinterest
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :rarible   {:title               "Rarible"
                :icon                :social/rarible
                :customization-color :rarible
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :snapchat  {:title               "Snapchat"
                :icon                :social/snapchat
                :customization-color :snapchat
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :spotify   {:title               "Spotify"
                :icon                :social/spotify
                :customization-color :spotify
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :superrare {:title               "SuperRare"
                :icon                :social/superrare
                :customization-color :superrare
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :tumblr    {:title               "Tumblr"
                :icon                :social/tumblr
                :customization-color :tumblr
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :twitch    {:title               "Twitch"
                :icon                :social/twitch
                :customization-color :twitch
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :twitter   {:title               "Twitter"
                :icon                :social/twitter
                :customization-color :twitter
                :on-press            #(js/alert "pressed")
                :address             "@francescab"}
    :youtube   {:title               "YouTube"
                :icon                :social/youtube
                :customization-color :youtube
                :on-press            #(js/alert "pressed")
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
       [quo/link-card (initial-state (:link @state))]])))
