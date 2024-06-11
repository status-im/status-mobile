(ns quo.components.avatars.dapp-avatar.style)

(defn- size->int
  [size]
  (case size
    :size-32 32
    :size-64 64
    32))

(defn container
  [size]
  (let [container-size (size->int size)]
    {:height container-size
     :width  container-size}))

(defn hole-view
  [size]
  (let [container-size (size->int size)]
    {:width  container-size
     :height container-size}))

(defn context
  [size]
  (let [context-size (case size
                       :size-32 16
                       :size-64 20
                       20)
        offset       (case size
                       :size-32 -4
                       :size-64 0
                       -4)]
    {:width         context-size
     :height        context-size
     :border-radius (/ context-size 2)
     :position      :absolute
     :right         offset
     :bottom        offset}))

(defn context-hole
  [size]
  (let [context-hole-size (case size
                            :size-32 18
                            :size-64 24
                            18)
        offset            (case size
                            :size-32 19
                            :size-64 42
                            19)]
    {:x            offset
     :y            offset
     :width        context-hole-size
     :height       context-hole-size
     :borderRadius (/ context-hole-size 2)}))

(defn image
  [size]
  (let [container-size (size->int size)]
    {:width  container-size
     :height container-size}))
