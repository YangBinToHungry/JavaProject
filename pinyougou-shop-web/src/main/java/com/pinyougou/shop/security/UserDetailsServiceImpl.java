package com.pinyougou.shop.security;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {
    //注入
    private SellerService sellerService;
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TbSeller seller = sellerService.findOne(username);
        if(seller==null){
            return null;
        }
        if(!"1".equals(seller.getStatus())){
            return null;
        }
        List<GrantedAuthority> grantedAuths  = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        return new User(username,seller.getPassword(),grantedAuths);
    }
}
