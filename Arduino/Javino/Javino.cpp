/*
  Javino.cpp - Library communication for Arduino and Jason.
Version Stable 1.1
  Created by Lazarin, NM and Pantoja, CE - January 29, 2015.
	nilson.lazarin@cefet-rj.br
	carlos.pantoja@cefet-rj.br

  Updated in 2022-06-18
  Released into the public domain.
*/

#include "Arduino.h"
#include "Javino.h"

Javino::Javino()
{

}

void Javino::sendmsg(String m)
{
	m = "fffe"+int2hex(m.length())+m;
	Serial.println(m);
}

String Javino::getmsg()
{
   return _finalymsg;
}

boolean Javino::availablemsg(){
    //_msg = false;
    inicializa();
	return _msg;
}

void Javino::inicializa(){
  _x         = 261;
  _d         = 0;
  _n         = 10;
  /*
  for( int i = 0; i < sizeofarraymsg;  ++i ){
    _arraymsg[i] = (char)0;
  }
  */
  listening();
}

void Javino::listening(){
  if(Serial.available()>0){
    registra();
  }
  else{
    timeout();
  }
}

void Javino::timeout(){
  delay(5);
  _n--;
  if(_n>0){
    listening();
  }else{
    aborta();
  }
}

void Javino::registra(){
    _arraymsg[_d]=Serial.read();
    _d++;
    _x--;
    _n=5;
    monitormsg();
}

void Javino::monitormsg(){

  if((_d==4)){
        if((_arraymsg[0]!='f')||(_arraymsg[1]!='f')||(_arraymsg[2]!='f')||(_arraymsg[3]!='e')){
                aborta();
        }
  }else if(_d==6){
    _x = sizeofmsg();
  }

  if(_x==0){
    tratamsg();
  }else{
    listening();
  }
}

void Javino::aborta(){
	_msg = false;
    _x=0;
}

int Javino::sizeofmsg(){
    int x = forInt(_arraymsg[5]);
    int y = forInt(_arraymsg[4]);
    int convertido = x+(y*16);
    return convertido;
}

void Javino::tratamsg(){
  if(preambulo()){
	_msg=true;
    _finalymsg = char2string(_arraymsg,_d);
  }else{
	_msg=false;
  }
}

boolean Javino::preambulo(){
    boolean out =false;
  if((_arraymsg[0]=='f')&&(_arraymsg[1]=='f')&&(_arraymsg[2]=='f')&&(_arraymsg[3]=='e')){
    out=true;
  }
  return out;
}


int Javino::forInt(char v){
  int vI=0;
  switch (v) {
    case '1': vI=1;  break;
    case '2': vI=2;  break;
    case '3': vI=3;  break;
    case '4': vI=4;  break;
    case '5': vI=5;  break;
    case '6': vI=6;  break;
    case '7': vI=7;  break;
    case '8': vI=8;  break;
    case '9': vI=9;  break;
    case 'a': vI=10; break;
    case 'b': vI=11; break;
    case 'c': vI=12; break;
    case 'd': vI=13; break;
    case 'e': vI=14; break;
    case 'f': vI=15; break;
  }
  return vI;
}

String Javino::char2string(char in[], int sizein){
  String saida;
  for(int i=6;i<sizein;i++){
    saida=saida+in[i];
  }
  return saida;
}


String Javino::int2hex(int v){
    String  stringOne =  String(v, HEX);
    if(v<16){
      stringOne="0"+stringOne;
    }
  return stringOne;
}

