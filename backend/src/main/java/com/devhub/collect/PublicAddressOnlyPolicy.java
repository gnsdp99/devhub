package com.devhub.collect;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;

/**
 * 공인 주소로만 요청을 허용한다. 피드 도메인이 만료되거나 탈취됐을 때 클라우드 메타데이터나 사내망으로 유도하는 리다이렉트를 막는다.
 */
@Component
public class PublicAddressOnlyPolicy implements FeedAddressPolicy {

    @Override
    public void check(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            throw new FeedFetchException("host를 해석할 수 없습니다: " + uri);
        }
        for (InetAddress address : resolve(host, uri)) {
            if (isInternal(address)) {
                throw new FeedFetchException("내부 주소로는 요청하지 않습니다: " + uri);
            }
        }
    }

    private InetAddress[] resolve(String host, URI uri) {
        try {
            return InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new FeedFetchException("host를 찾을 수 없습니다: " + uri, e);
        }
    }

    private boolean isInternal(InetAddress address) {
        return address.isLoopbackAddress()
                || address.isAnyLocalAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()
                || isUniqueLocal(address);
    }

    /**
     * IPv6 unique local(fc00::/7). {@link InetAddress#isSiteLocalAddress()}가 잡지 못한다.
     */
    private boolean isUniqueLocal(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 16 && (bytes[0] & 0xFE) == 0xFC;
    }
}