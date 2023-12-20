(ns status-im.contexts.wallet.collectible.tabs.about.view
  (:require [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [status-im.contexts.wallet.collectible.tabs.about.style :as style]
            [utils.re-frame :as rf]))

(def link-cards
  [{:title               "BAYC"
    :icon                :social/link
    :address             "boredapeyachtclub"
    :customization-color :social/link
    :on-press            #(js/alert "pressed")}
   {:title               "Twitter"
    :icon                :social/twitter
    :address             "@BoredApeYC"
    :customization-color :social/twitter
    :on-press            #(js/alert "pressed")}
   {:title               "Opensea"
    :icon                :social/opensea
    :address             "Bored Ape Yacht Club"
    :customization-color :social/opensea
    :on-press            #(js/alert "pressed")}])

(defn- view-internal
  []
  (let [window-width (rf/sub [:dimensions/window-width])
        item-width   (- (/ window-width 2) 28)]
    [:<>
     [rn/view {:style style/title}
      [quo/text
       {:size   :heading-2
        :weight :semi-bold}
       "Bored Ape Yacht Club"]]
     [rn/view {:style style/description}
      [quo/text
       {:size :paragraph-2}
       "The Bored Ape Yacht Club is a collection of 10,000 unique Bored Ape NFTs— unique digital collectibles living on the Ethereum blockchain. Your Bored Ape doubles as your Yacht Club membership card, and grants access to members-only benefits, the first of which is access to THE BATHROOM, a collaborative graffiti board. Future areas and perks can be unlocked by the community through roadmap activation. Visit www.BoredApeYachtClub.com for more details."]]
     [quo/section-label
      {:container-style style/section-label
       :section         "On the web"}]
     [rn/view {:style style/link-cards-container}
      (for [item link-cards]
        ^{:key (:title item)}
        [quo/link-card (assoc item :container-style (style/link-card item-width))])]]))

(def view (quo.theme/with-theme view-internal))
