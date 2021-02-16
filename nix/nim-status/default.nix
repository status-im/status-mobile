{ newScope
, writeScript
, xcodeWrapper
, pkgs
, stdenv
, lib }:
let
  callPackage = newScope {};

in rec {
  srcRaw = pkgs.fetchgit {
    url = "https://github.com/status-im/nim-status";
    rev = "13641d4f9d8fbe942cb71c049df9a0bda1fdfa1c";
    sha256 = "0mb2fdjps4qv1rv5l5fp0bgxaikgf3zssbsmwsh6g5biysrbmzys";
    fetchSubmodules = true;
  };

  # srcRaw = stdenv.mkDerivation rec {
  #   name = "nim-status-src-builder";
  #   buildInputs = [ pkgs.coreutils pkgs.cacert pkgs.git ];
  #   rev = "03cac5fa6d7fbd45b16307dd22d461a48585c8a9";
  #   GIT_SSL_CAINFO = "${pkgs.cacert}/etc/ssl/certs/ca-bundle.crt";
  #   builder = writeScript "nim-status-src-builder.sh"
  #   ''
  #     export PATH=${pkgs.coreutils}/bin:${pkgs.git}/bin:$PATH
  #     git clone https://github.com/status-im/nim-status $out
  #     cd $out
  #     git checkout ${rev}
  #   	git submodule update --init --recursive || true
	    # git submodule sync --quiet --recursive
	    # git submodule update --init --recursive
  #   '';

  # };

  sqlcipher = callPackage ./sqlcipher.nix { platform = "android"; arch = "386"; };
  nim-sqlcipher = callPackage ./nim-sqlcipher.nix { platform = "android"; arch = "386"; };
  nim-status = callPackage ./nim-status.nix { platform = "android"; arch = "386"; };

  openssl-android-x86 = callPackage ./openssl.nix { platform = "android"; arch = "386"; };
  openssl-ios-x86 = callPackage ./openssl.nix { platform = "ios"; arch = "386"; };

  openssl-android-arm = callPackage ./openssl.nix { platform = "androideabi"; arch = "arm"; };
  openssl-ios-arm = callPackage ./openssl.nix { platform = "ios"; arch = "arm"; };

  openssl-android-arm64 = callPackage ./openssl.nix { platform = "android"; arch = "arm64"; };
  openssl-ios-arm64 = callPackage ./openssl.nix { platform = "ios"; arch = "arm64"; };

  pcre-android-x86 = callPackage ./pcre.nix { platform = "android"; arch = "386"; };
  pcre-ios-x86 = callPackage ./pcre.nix { platform = "ios"; arch = "386"; };

  pcre-android-arm = callPackage ./pcre.nix { platform = "androideabi"; arch = "arm"; };
  pcre-ios-arm = callPackage ./pcre.nix { platform = "ios"; arch = "arm"; };

  pcre-android-arm64 = callPackage ./pcre.nix { platform = "android"; arch = "arm64"; };
  pcre-ios-arm64 = callPackage ./pcre.nix { platform = "ios"; arch = "arm64"; };



  android-x86 = callPackage ./build.nix { 
    inherit srcRaw; 
    platform = "android"; 
    arch = "386"; 
    openssl = openssl-android-x86;
    pcre = pcre-android-x86;
  };
  android-arm = callPackage ./build.nix { 
    inherit srcRaw;
    platform = "androideabi"; 
    arch = "arm";
    openssl = openssl-android-arm;
    pcre = pcre-android-arm;
  };
  android-arm64 = callPackage ./build.nix {
    inherit srcRaw; 
    platform = "android"; 
    arch = "arm64";
    openssl = openssl-android-arm64;
    pcre = pcre-android-arm64;
  };

  ios-x86 = callPackage ./build.nix { 
    inherit srcRaw; 
    platform = "ios"; 
    arch = "386"; 
    openssl = openssl-ios-x86;
    pcre = pcre-ios-x86;
  };
  ios-arm = callPackage ./build.nix {
    inherit srcRaw; 
    platform = "ios"; 
    arch = "arm"; 
    openssl = openssl-ios-arm;
    pcre = pcre-ios-arm;
  };
  ios-arm64 = callPackage ./build.nix {
    inherit srcRaw; 
    platform = "ios"; 
    arch = "arm64"; 
    openssl = openssl-ios-arm64;
    pcre = pcre-ios-arm64;
  };

  android = stdenv.mkDerivation {
    name = "nim-status-android-builder";
    buildInputs = [ pkgs.coreutils ];
    builder = writeScript "nim-status-android-builder.sh"
    ''
      export PATH=${pkgs.coreutils}/bin:$PATH
      mkdir -p $out/x86
      mkdir $out/armeabi-v7a
      mkdir $out/arm64-v8a

      ln -s ${android-x86}/* $out/x86
      ln -s ${openssl-android-x86}/lib/libcrypto.a $out/x86/libcrypto.a
      ln -s ${openssl-android-x86}/lib/libssl.a $out/x86/libssl.a
      ln -s ${pcre-android-x86}/lib/libpcre.a $out/x86/libpcre.a

      ln -s ${android-arm}/* $out/armeabi-v7a
      ln -s ${openssl-android-arm}/lib/libcrypto.a $out/armeabi-v7a/libcrypto.a
      ln -s ${openssl-android-arm}/lib/libssl.a $out/armeabi-v7a/libssl.a
      ln -s ${pcre-android-arm}/lib/libpcre.a $out/armeabi-v7a/libpcre.a


      ln -s ${android-arm64}/* $out/arm64-v8a
      ln -s ${openssl-android-arm64}/lib/libcrypto.a $out/arm64-v8a/libcrypto.a
      ln -s ${openssl-android-arm64}/lib/libssl.a $out/arm64-v8a/libssl.a
      ln -s ${pcre-android-arm64}/lib/libpcre.a $out/arm64-v8a/libpcre.a
    '';
  };

  ios = stdenv.mkDerivation {
    inherit xcodeWrapper;
    buildInputs = [ pkgs.coreutils ];
    name = "nim-status-ios-builder";
    builder = writeScript "nim-status-ios-builder.sh"
    ''
      export PATH=${pkgs.coreutils}/bin:$PATH
      mkdir -p $out
      export PATH=${xcodeWrapper}/bin:$PATH 

      # lipo merges arch-specific binaries into one fat iOS binary
      lipo -create ${ios-x86}/libnim_status.a \
           ${ios-arm}/libnim_status.a \
           ${ios-arm64}/libnim_status.a \
           -output $out/libnim_status.a

      lipo -create ${openssl-ios-x86}/lib/libssl.a \
           ${openssl-ios-arm}/lib/libssl.a \
           ${openssl-ios-arm64}/lib/libssl.a \
           -output $out/libssl.a

      lipo -create ${openssl-ios-x86}/lib/libcrypto.a \
           ${openssl-ios-arm}/lib/libcrypto.a \
           ${openssl-ios-arm64}/lib/libcrypto.a \
           -output $out/libcrypto.a

      lipo -create ${pcre-ios-x86}/lib/libpcre.a \
           ${pcre-ios-arm}/lib/libpcre.a \
           ${pcre-ios-arm64}/lib/libpcre.a \
           -output $out/libpcre.a

      echo -e "#if TARGET_CPU_X86_64\n" >> $out/nim_status.h
      cat ${ios-x86}/nim_status.h >> $out/nim_status.h
      echo -e "#elif TARGET_CPU_ARM\n" >> $out/nim_status.h
      cat ${ios-arm}/nim_status.h >> $out/nim_status.h
      echo -e "#else \n" >> $out/nim_status.h
      cat ${ios-arm64}/nim_status.h >> $out/nim_status.h
      echo -e "#endif\n" >> $out/nim_status.h
      cp -r ${ios-arm64}/nimbase.h $out
    '';
  };
}

